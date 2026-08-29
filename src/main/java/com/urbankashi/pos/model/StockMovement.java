package com.urbankashi.pos.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_movements")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StockMovement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "variant_id", nullable = false) private ProductVariant variant;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private StockMovementType type;
    @Column(nullable = false) private Integer quantityChange;
    @Column(nullable = false) private Integer quantityAfter;
    @Column(length = 100) private String reference;
    @Column(length = 500) private String reason;
    @Column(length = 100) private String performedBy;
    @Column(updatable = false, nullable = false) private LocalDateTime createdAt;
    @PrePersist protected void onCreate() { createdAt = LocalDateTime.now(); }
}
