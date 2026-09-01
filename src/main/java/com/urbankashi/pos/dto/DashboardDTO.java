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

    // --- AUTO-GENERATED EXPLICIT ACCESSORS FOR JAVA 25 ---
    public BigDecimal getTodayRevenue() { return this.todayRevenue; }
    public void setTodayRevenue(BigDecimal todayRevenue) { this.todayRevenue = todayRevenue; }
    public Long getTodayTransactions() { return this.todayTransactions; }
    public void setTodayTransactions(Long todayTransactions) { this.todayTransactions = todayTransactions; }
    public BigDecimal getMonthRevenue() { return this.monthRevenue; }
    public void setMonthRevenue(BigDecimal monthRevenue) { this.monthRevenue = monthRevenue; }
    public Long getMonthTransactions() { return this.monthTransactions; }
    public void setMonthTransactions(Long monthTransactions) { this.monthTransactions = monthTransactions; }
    public Long getTotalCustomers() { return this.totalCustomers; }
    public void setTotalCustomers(Long totalCustomers) { this.totalCustomers = totalCustomers; }
    public Long getLowStockCount() { return this.lowStockCount; }
    public void setLowStockCount(Long lowStockCount) { this.lowStockCount = lowStockCount; }
    public List<Invoice> getRecentInvoices() { return this.recentInvoices; }
    public void setRecentInvoices(List<Invoice> recentInvoices) { this.recentInvoices = recentInvoices; }
    public List<ProductVariant> getLowStockItems() { return this.lowStockItems; }
    public void setLowStockItems(List<ProductVariant> lowStockItems) { this.lowStockItems = lowStockItems; }

}
