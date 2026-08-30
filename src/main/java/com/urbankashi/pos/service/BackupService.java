package com.urbankashi.pos.service;

import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.sql.DataSource;
import java.io.*;
import java.nio.file.*;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.Statement;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.GZIPOutputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class BackupService {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmss");
    private final DataSource dataSource;
    private final Environment environment;
    private final AuditService auditService;
    @Value("${app.backup.enabled:true}") private boolean enabled;
    @Value("${app.backup.directory}") private String backupDirectory;
    @Value("${app.backup.encryption-key:}") private String encryptionKey;
    @Value("${app.backup.google-service-account-key:}") private String serviceAccountKey;
    @Value("${app.backup.google-folder-id:}") private String googleFolderId;
    @Value("${app.backup.pg-dump-path:pg_dump}") private String pgDumpPath;
    @Getter private volatile BackupStatus lastStatus = BackupStatus.notRun();

    @PostConstruct
    void ensureDirectory() throws IOException { Files.createDirectories(Paths.get(backupDirectory)); }

    @Scheduled(cron = "0 30 23 * * *", zone = "Asia/Kolkata")
    public void dailyBackup() { runScheduled("daily", 30); }
    @Scheduled(cron = "0 35 23 * * SUN", zone = "Asia/Kolkata")
    public void weeklyBackup() { runScheduled("weekly", 12); }
    @Scheduled(cron = "0 40 23 1 * *", zone = "Asia/Kolkata")
    public void monthlyBackup() { runScheduled("monthly", 12); }
    @Scheduled(cron = "0 45 23 1 1,4,7,10 *", zone = "Asia/Kolkata")
    public void quarterlyBackup() { runScheduled("quarterly", 8); }
    @Scheduled(cron = "0 50 23 1 1 *", zone = "Asia/Kolkata")
    public void yearlyBackup() { runScheduled("yearly", 84); }

    public synchronized BackupStatus createBackup(String type) {
        if (!enabled) return lastStatus = BackupStatus.failed("Backups are disabled");
        if (encryptionKey == null || encryptionKey.length() < 16) return lastStatus = BackupStatus.failed("Set a BACKUP_ENCRYPTION_KEY with at least 16 characters before running backups");
        try {
            Path directory = Paths.get(backupDirectory); Files.createDirectories(directory);
            String name = "urban-kashi-" + type + "-" + LocalDateTime.now().format(FORMAT);
            Path dump = directory.resolve(name + ".sql");
            exportDatabase(dump);
            Path encrypted = encryptAndCompress(dump, directory.resolve(name + ".sql.gz.enc"));
            Files.deleteIfExists(dump);
            String driveFileId = uploadToGoogleDrive(encrypted);
            BackupStatus status = BackupStatus.success(encrypted.getFileName().toString(), Files.size(encrypted), driveFileId);
            lastStatus = status;
            auditService.record("BACKUP_COMPLETED", "Backup", type, status.message());
            return status;
        } catch (Exception exception) {
            log.error("{} backup failed", type, exception);
            lastStatus = BackupStatus.failed(exception.getMessage());
            auditService.record("BACKUP_FAILED", "Backup", type, safeMessage(exception));
            return lastStatus;
        }
    }

    private void runScheduled(String type, int retention) {
        BackupStatus status = createBackup(type);
        if (status.success()) prune(type, retention);
    }

    private void exportDatabase(Path target) throws Exception {
        String url;
        try (Connection connection = dataSource.getConnection()) { url = connection.getMetaData().getURL(); }
        if (url.startsWith("jdbc:h2:")) {
            try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
                statement.execute("SCRIPT TO '" + target.toAbsolutePath().toString().replace("'", "''") + "'");
            }
            return;
        }
        if (!url.startsWith("jdbc:postgresql:")) throw new IllegalStateException("Only PostgreSQL and H2 backups are supported");
        URIConnection details = parsePostgresUrl(url);
        ProcessBuilder process = new ProcessBuilder(pgDumpPath, "--host=" + details.host(), "--port=" + details.port(), "--username=" + details.user(), "--format=plain", "--no-owner", "--file=" + target, details.database());
        String password = environment.getProperty("spring.datasource.password", "");
        process.environment().put("PGPASSWORD", password);
        process.redirectErrorStream(true);
        Process running = process.start();
        String output;
        try (InputStream stream = running.getInputStream()) { output = new String(stream.readAllBytes()); }
        if (running.waitFor() != 0) throw new IllegalStateException("pg_dump failed: " + output);
    }

    private Path encryptAndCompress(Path source, Path target) throws Exception {
        byte[] salt = new byte[16], nonce = new byte[12]; SecureRandom random = new SecureRandom(); random.nextBytes(salt); random.nextBytes(nonce);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(new PBEKeySpec(encryptionKey.toCharArray(), salt, 210_000, 256)).getEncoded();
        SecretKey key = new javax.crypto.spec.SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding"); cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, nonce));
        try (OutputStream file = Files.newOutputStream(target); DataOutputStream header = new DataOutputStream(file)) {
            header.writeUTF("UKB1"); header.write(salt); header.write(nonce);
            try (GZIPOutputStream gzip = new GZIPOutputStream(new javax.crypto.CipherOutputStream(header, cipher)); InputStream input = Files.newInputStream(source)) { input.transferTo(gzip); }
        }
        return target;
    }

    private String uploadToGoogleDrive(Path backup) throws IOException {
        if (serviceAccountKey == null || serviceAccountKey.isBlank() || googleFolderId == null || googleFolderId.isBlank()) return "LOCAL_ONLY";
        try (InputStream key = Files.newInputStream(Paths.get(serviceAccountKey))) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(key).createScoped(DriveScopes.DRIVE_FILE);
            Drive drive = new Drive.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance(), new HttpCredentialsAdapter(credentials)).setApplicationName("Urban Kashi POS Backup").build();
            File metadata = new File().setName(backup.getFileName().toString()).setParents(List.of(googleFolderId));
            File uploaded = drive.files().create(metadata, new FileContent("application/octet-stream", backup.toFile())).setFields("id").execute();
            return uploaded.getId();
        }
    }

    private void prune(String type, int retention) {
        try (var files = Files.list(Paths.get(backupDirectory))) {
            files.filter(path -> path.getFileName().toString().startsWith("urban-kashi-" + type + "-") && path.toString().endsWith(".enc"))
                    .sorted(Comparator.comparing(Path::getFileName).reversed()).skip(retention).forEach(path -> { try { Files.deleteIfExists(path); } catch (IOException ignored) { } });
        } catch (IOException exception) { log.warn("Unable to prune {} backups", type, exception); }
    }

    private URIConnection parsePostgresUrl(String url) {
        String value = url.substring("jdbc:postgresql://".length()); String[] hostAndDatabase = value.split("/", 2);
        String[] hostPort = hostAndDatabase[0].split(":", 2); String database = hostAndDatabase[1].split("\\?", 2)[0];
        return new URIConnection(hostPort[0], hostPort.length > 1 ? hostPort[1] : "5432", database, environment.getProperty("spring.datasource.username", "postgres"));
    }
    private String safeMessage(Exception exception) { return exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage(); }
    private record URIConnection(String host, String port, String database, String user) { }
    public record BackupStatus(boolean success, String fileName, long size, String driveFileId, String message, LocalDateTime createdAt) {
        static BackupStatus success(String file, long size, String driveId) { return new BackupStatus(true, file, size, driveId, "Backup completed", LocalDateTime.now()); }
        static BackupStatus failed(String message) { return new BackupStatus(false, null, 0, null, message, LocalDateTime.now()); }
        static BackupStatus notRun() { return new BackupStatus(false, null, 0, null, "No backup has run yet", null); }
    }
}
