package com.urbankashi.pos.service;

import com.urbankashi.pos.dto.DashboardDTO;
import com.urbankashi.pos.model.Invoice;
import com.urbankashi.pos.model.ProductVariant;
import com.urbankashi.pos.repository.CustomerRepository;
import com.urbankashi.pos.repository.InvoiceRepository;
import com.urbankashi.pos.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final InvoiceRepository invoiceRepository;
    private final ProductVariantRepository variantRepository;
    private final CustomerRepository customerRepository;

    public DashboardDTO getDashboardData() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        BigDecimal todayRevenue = invoiceRepository.sumRevenueSince(todayStart);
        if (todayRevenue == null) todayRevenue = BigDecimal.ZERO;
        long todayTransactions = invoiceRepository.countInvoicesSince(todayStart);

        BigDecimal monthRevenue = invoiceRepository.sumRevenueSince(monthStart);
        if (monthRevenue == null) monthRevenue = BigDecimal.ZERO;
        long monthTransactions = invoiceRepository.countInvoicesSince(monthStart);

        long totalCustomers = customerRepository.count();

        List<ProductVariant> lowStockItems = variantRepository.findLowStock(5);

        List<Invoice> recentInvoices = invoiceRepository.findTop10ByOrderByCreatedAtDesc();

        DashboardDTO dto = new DashboardDTO();
        dto.setTodayRevenue(todayRevenue);
        dto.setTodayTransactions(todayTransactions);
        dto.setMonthRevenue(monthRevenue);
        dto.setMonthTransactions(monthTransactions);
        dto.setTotalCustomers(totalCustomers);
        dto.setLowStockItems(lowStockItems);
        dto.setLowStockCount((long) lowStockItems.size());
        dto.setRecentInvoices(recentInvoices);

        return dto;
    }
}
