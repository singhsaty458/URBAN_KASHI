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
}
