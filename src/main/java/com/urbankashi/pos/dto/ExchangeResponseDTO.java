package com.urbankashi.pos.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data @Builder
public class ExchangeResponseDTO {
    private Long exchangeId;
    private String exchangeNumber;
    private String originalInvoiceNumber;
    private BigDecimal replacementCredit;
    private BigDecimal amountPaid;
    private InvoiceResponseDTO replacementInvoice;
}
