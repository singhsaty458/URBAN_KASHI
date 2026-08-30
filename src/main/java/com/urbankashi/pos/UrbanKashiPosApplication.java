package com.urbankashi.pos;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.Bean;

import java.net.InetAddress;
import jakarta.annotation.PostConstruct;
import java.util.TimeZone;


@SpringBootApplication
@EnableScheduling
public class UrbanKashiPosApplication {

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
    }

    public static void main(String[] args) {
        SpringApplication.run(UrbanKashiPosApplication.class, args);
    }

    @Bean
    public CommandLineRunner printStartupBanner() {
        return args -> {
            String ipAddress = "localhost";
            try {
                ipAddress = InetAddress.getLocalHost().getHostAddress();
            } catch (Exception e) {
                // Ignore and fallback to localhost
            }
            System.out.println("========================================");
            System.out.println("  URBAN KASHI POS - Running!");
            System.out.println("  Access: http://localhost:8080");
            System.out.println("  LAN: http://" + ipAddress + ":8080");
            System.out.println("========================================");
        };
    }
}

