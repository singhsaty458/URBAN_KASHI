package com.urbankashi.pos.dto;

import com.urbankashi.pos.model.Invoice;
import com.urbankashi.pos.model.ProductVariant;
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
public class DashboardDTO {
    private BigDecimal todayRevenue;
    private Long todayTransactions;
    private BigDecimal monthRevenue;
    private Long monthTransactions;
    private Long totalCustomers;
    private Long lowStockCount;
    private List<Invoice> recentInvoices;
    private List<ProductVariant> lowStockItems;
}
