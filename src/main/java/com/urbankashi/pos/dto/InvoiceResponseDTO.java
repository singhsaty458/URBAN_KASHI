package com.urbankashi.pos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceResponseDTO {
    private Long invoiceId;
    private String invoiceNumber;
    private String customerName;
    private String customerPhone;
    private List<ItemDetail> items;
    private BigDecimal totalTaxable;
    private BigDecimal totalCgst;
    private BigDecimal totalSgst;
    private BigDecimal grandTotal;
    private BigDecimal discount;
    private String paymentMode;
    private String createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemDetail {
        private String productName;
        private String size;
        private String color;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal taxableAmount;
        private BigDecimal cgst;
        private BigDecimal sgst;
        private BigDecimal total;
    }
}
