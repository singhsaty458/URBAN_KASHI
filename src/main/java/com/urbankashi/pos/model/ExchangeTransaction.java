package com.urbankashi.pos.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exchange_transactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ExchangeTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 40) private String exchangeNumber;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "original_invoice_id") private Invoice originalInvoice;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "replacement_invoice_id") private Invoice replacementInvoice;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal replacementCredit;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal amountPaid;
    @Column(length = 500) private String reason;
    @Column(length = 100) private String performedBy;
    @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
    @OneToMany(mappedBy = "exchangeTransaction", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default private List<ExchangeItem> items = new ArrayList<>();
    @PrePersist void onCreate() { createdAt = LocalDateTime.now(); }
}
