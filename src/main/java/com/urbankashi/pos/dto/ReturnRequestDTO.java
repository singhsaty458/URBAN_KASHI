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
}
