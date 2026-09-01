package com.urbankashi.pos.dto;
import lombok.Data;
import java.util.List;
import java.util.ArrayList;
@Data
public class BulkPrintFormDTO {
    private List<BulkPrintRequestDTO> items = new ArrayList<>();
}