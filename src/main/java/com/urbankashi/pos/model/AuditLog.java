package com.urbankashi.pos.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 80) private String action;
    @Column(nullable = false, length = 80) private String entityType;
    @Column(length = 80) private String entityId;
    @Column(length = 100) private String performedBy;
    @Column(length = 500) private String details;
    @Column(updatable = false, nullable = false) private LocalDateTime createdAt;
    @PrePersist protected void onCreate() { createdAt = LocalDateTime.now(); }
}
