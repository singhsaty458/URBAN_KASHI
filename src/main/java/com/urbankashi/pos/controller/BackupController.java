package com.urbankashi.pos.controller;

import com.urbankashi.pos.service.BackupService;
import com.urbankashi.pos.service.DataExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class BackupController {
    private final BackupService backupService;
    private final DataExportService dataExportService;

    @GetMapping("/backups")
    public String backups(Model model) {
        BackupService.BackupStatus backup = backupService.getLastStatus();
        model.addAttribute("backupSuccess", backup.success());
        model.addAttribute("backupMessage", backup.message());
        model.addAttribute("backupCreatedAt", backup.createdAt());
        model.addAttribute("backupFileName", backup.fileName());
        model.addAttribute("backupDriveFileId", backup.driveFileId());
        return "backups";
    }

    @PostMapping("/backups/run")
    public String runBackup(@RequestParam(defaultValue = "manual") String type) {
        backupService.createBackup(type);
        return "redirect:/backups";
    }

    @GetMapping("/backups/export")
    public ResponseEntity<byte[]> exportData(@RequestParam String tab, @RequestParam String format,
                                             @RequestParam(required = false) String period,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        LocalDate endDate = to == null ? LocalDate.now() : to;
        LocalDate startDate = from == null ? periodStart(period) : from;
        DataExportService.ExportFile file = dataExportService.export(tab, format, startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay());
        return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=\"" + file.fileName() + "\"")
                .header("Content-Type", file.contentType()).body(file.bytes());
    }

    private LocalDate periodStart(String period) {
        LocalDate now = LocalDate.now();
        return switch (period == null ? "monthly" : period) {
            case "1day" -> now.minusDays(1);
            case "3days" -> now.minusDays(3);
            case "weekly" -> now.minusWeeks(1);
            case "yearly" -> now.minusYears(1);
            default -> now.minusMonths(1);
        };
    }
}
