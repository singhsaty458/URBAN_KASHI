package com.urbankashi.pos.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class ReturnResponseDTO {
    Long returnId;
    String returnNumber;
    String invoiceNumber;
    BigDecimal refundAmount;
    String refundMode;
    String status;
}
