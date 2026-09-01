package com.urbankashi.pos.dto;
import lombok.Data;
@Data
public class BulkPrintRequestDTO {
    private Long variantId;
    private int quantity;
}