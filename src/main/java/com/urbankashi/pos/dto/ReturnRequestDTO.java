package com.urbankashi.pos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ReturnRequestDTO {
    @NotBlank
    private String invoiceNumber;
    @NotEmpty
    @Valid
    private List<ReturnItemRequestDTO> items;
    private String refundMode;
    private String reason;

    // --- AUTO-GENERATED EXPLICIT ACCESSORS FOR JAVA 25 ---
    public String getInvoiceNumber() { return this.invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public List<ReturnItemRequestDTO> getItems() { return this.items; }
    public void setItems(List<ReturnItemRequestDTO> items) { this.items = items; }
    public String getRefundMode() { return this.refundMode; }
    public void setRefundMode(String refundMode) { this.refundMode = refundMode; }
    public String getReason() { return this.reason; }
    public void setReason(String reason) { this.reason = reason; }

}
