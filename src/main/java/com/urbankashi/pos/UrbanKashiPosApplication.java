package com.urbankashi.pos;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.net.InetAddress;

@SpringBootApplication
public class UrbanKashiPosApplication {

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
