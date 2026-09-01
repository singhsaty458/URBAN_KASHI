package com.urbankashi.pos.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "invoice_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    @JsonIgnore
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = true)
    private ProductVariant variant;

    private String productName;
    private String size;
    private String color;
    private Integer quantity;

    @Column(precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(precision = 10, scale = 2)
    private BigDecimal costPrice;

    @Column(precision = 10, scale = 2)
    private BigDecimal taxableAmount;

    @Column(precision = 10, scale = 2)
    private BigDecimal cgstAmount;

    @Column(precision = 10, scale = 2)
    private BigDecimal sgstAmount;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalAmount;

    // --- AUTO-GENERATED EXPLICIT ACCESSORS FOR JAVA 25 ---
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public Invoice getInvoice() { return this.invoice; }
    public void setInvoice(Invoice invoice) { this.invoice = invoice; }
    public ProductVariant getVariant() { return this.variant; }
    public void setVariant(ProductVariant variant) { this.variant = variant; }
    public String getProductName() { return this.productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getSize() { return this.size; }
    public void setSize(String size) { this.size = size; }
    public String getColor() { return this.color; }
    public void setColor(String color) { this.color = color; }
    public Integer getQuantity() { return this.quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return this.unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getCostPrice() { return this.costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }
    public BigDecimal getTaxableAmount() { return this.taxableAmount; }
    public void setTaxableAmount(BigDecimal taxableAmount) { this.taxableAmount = taxableAmount; }
    public BigDecimal getCgstAmount() { return this.cgstAmount; }
    public void setCgstAmount(BigDecimal cgstAmount) { this.cgstAmount = cgstAmount; }
    public BigDecimal getSgstAmount() { return this.sgstAmount; }
    public void setSgstAmount(BigDecimal sgstAmount) { this.sgstAmount = sgstAmount; }
    public BigDecimal getTotalAmount() { return this.totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

}
