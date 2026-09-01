package com.urbankashi.pos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 50)
    private String invoiceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalTaxable;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalCgst;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalSgst;

    @Column(precision = 10, scale = 2)
    private BigDecimal grandTotal;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private PaymentMode paymentMode;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "COMPLETED";

    @Column(length = 100, updatable = false)
    private String cashierUsername;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<InvoiceItem> items = new ArrayList<>();

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // --- AUTO-GENERATED EXPLICIT ACCESSORS FOR JAVA 25 ---
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public String getInvoiceNumber() { return this.invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public Customer getCustomer() { return this.customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public BigDecimal getTotalTaxable() { return this.totalTaxable; }
    public void setTotalTaxable(BigDecimal totalTaxable) { this.totalTaxable = totalTaxable; }
    public BigDecimal getTotalCgst() { return this.totalCgst; }
    public void setTotalCgst(BigDecimal totalCgst) { this.totalCgst = totalCgst; }
    public BigDecimal getTotalSgst() { return this.totalSgst; }
    public void setTotalSgst(BigDecimal totalSgst) { this.totalSgst = totalSgst; }
    public BigDecimal getGrandTotal() { return this.grandTotal; }
    public void setGrandTotal(BigDecimal grandTotal) { this.grandTotal = grandTotal; }
    public BigDecimal getDiscount() { return this.discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }
    public PaymentMode getPaymentMode() { return this.paymentMode; }
    public void setPaymentMode(PaymentMode paymentMode) { this.paymentMode = paymentMode; }
    public String getStatus() { return this.status; }
    public void setStatus(String status) { this.status = status; }
    public String getCashierUsername() { return this.cashierUsername; }
    public void setCashierUsername(String cashierUsername) { this.cashierUsername = cashierUsername; }
    public List<InvoiceItem> getItems() { return this.items; }
    public void setItems(List<InvoiceItem> items) { this.items = items; }
    public LocalDateTime getCreatedAt() { return this.createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

}
