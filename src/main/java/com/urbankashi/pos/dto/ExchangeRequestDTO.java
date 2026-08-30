package com.urbankashi.pos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class ExchangeRequestDTO {
    @NotBlank private String invoiceNumber;
    @Valid @NotEmpty private List<ReturnItemRequestDTO> returnedItems;
    @Valid @NotEmpty private List<CartItemDTO> replacementItems;
    @NotBlank private String paymentMode;
    private String reason;
}
