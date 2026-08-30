package com.urbankashi.pos.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "exchange_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ExchangeItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "exchange_transaction_id") private ExchangeTransaction exchangeTransaction;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "original_invoice_item_id") private InvoiceItem originalInvoiceItem;
    @Column(nullable = false) private Integer quantity;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal creditAmount;
}
