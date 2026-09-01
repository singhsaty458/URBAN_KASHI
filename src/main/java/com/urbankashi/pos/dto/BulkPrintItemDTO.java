package com.urbankashi.pos.dto;

import com.urbankashi.pos.model.ProductVariant;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BulkPrintItemDTO {
    private ProductVariant variant;
    private String qrCodeBase64;
    private int quantity;
}