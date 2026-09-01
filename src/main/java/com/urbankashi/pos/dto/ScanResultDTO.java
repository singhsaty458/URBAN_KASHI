package com.urbankashi.pos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScanResultDTO {
    private Long variantId;
    private String productName;
    private String brand;
    private String size;
    private String color;
    private String barcode;
    private BigDecimal sellingPrice;
    private Integer stockQuantity;
    private BigDecimal gstRate;
    private String hsnCode;
    private String imageUrl;

    // --- AUTO-GENERATED EXPLICIT ACCESSORS FOR JAVA 25 ---
    public Long getVariantId() { return this.variantId; }
    public void setVariantId(Long variantId) { this.variantId = variantId; }
    public String getProductName() { return this.productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getBrand() { return this.brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getSize() { return this.size; }
    public void setSize(String size) { this.size = size; }
    public String getColor() { return this.color; }
    public void setColor(String color) { this.color = color; }
    public String getBarcode() { return this.barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public BigDecimal getSellingPrice() { return this.sellingPrice; }
    public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }
    public Integer getStockQuantity() { return this.stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    public BigDecimal getGstRate() { return this.gstRate; }
    public void setGstRate(BigDecimal gstRate) { this.gstRate = gstRate; }
    public String getHsnCode() { return this.hsnCode; }
    public void setHsnCode(String hsnCode) { this.hsnCode = hsnCode; }
    public String getImageUrl() { return this.imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

}
