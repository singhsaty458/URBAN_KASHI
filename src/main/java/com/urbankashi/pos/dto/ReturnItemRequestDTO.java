package com.urbankashi.pos.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ReturnItemRequestDTO {
    @NotNull
    private Long invoiceItemId;
    @NotNull
    @Positive
    private Integer quantity;

    // --- AUTO-GENERATED EXPLICIT ACCESSORS FOR JAVA 25 ---
    public Long getInvoiceItemId() { return this.invoiceItemId; }
    public void setInvoiceItemId(Long invoiceItemId) { this.invoiceItemId = invoiceItemId; }
    public Integer getQuantity() { return this.quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

}
