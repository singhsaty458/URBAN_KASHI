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
}
