package com.urbankashi.pos.controller;

import com.urbankashi.pos.model.AuditLog;
import com.urbankashi.pos.model.StockMovement;
import com.urbankashi.pos.model.StockMovementType;
import com.urbankashi.pos.repository.AuditLogRepository;
import com.urbankashi.pos.repository.StockMovementRepository;
import com.urbankashi.pos.repository.InvoiceRepository;
import com.urbankashi.pos.model.Invoice;
import com.urbankashi.pos.model.PaymentMode;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class OperationsController {
    private final StockMovementRepository stockMovementRepository;
    private final AuditLogRepository auditLogRepository;
    private final InvoiceRepository invoiceRepository;

    @GetMapping("/stock-history")
    public String stockHistory(@RequestParam(required = false) StockMovementType type,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                               Model model) {
        LocalDateTime start = (from == null ? LocalDate.now().minusDays(30) : from).atStartOfDay();
        LocalDateTime end = (to == null ? LocalDate.now() : to).plusDays(1).atStartOfDay();
        List<StockMovement> movements = type == null
                ? stockMovementRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end)
                : stockMovementRepository.findByTypeAndCreatedAtBetweenOrderByCreatedAtDesc(type, start, end);
        model.addAttribute("movements", movements);
        model.addAttribute("types", StockMovementType.values());
        model.addAttribute("selectedType", type);
        model.addAttribute("from", start.toLocalDate());
        model.addAttribute("to", end.minusDays(1).toLocalDate());
        return "stock-history";
    }

    @GetMapping(value = "/stock-history/export", produces = "text/csv")
    @ResponseBody
    public byte[] exportStockHistory() {
        StringBuilder csv = new StringBuilder("Date,Type,Product,Variant,Change,After,Reference,Reason,User\n");
        stockMovementRepository.findTop100ByOrderByCreatedAtDesc().forEach(m -> csv.append(csv(m.getCreatedAt())).append(',')
                .append(csv(m.getType())).append(',').append(csv(m.getVariant().getProduct().getName())).append(',')
                .append(csv(m.getVariant().getSize() + " / " + m.getVariant().getColor())).append(',')
                .append(m.getQuantityChange()).append(',').append(m.getQuantityAfter()).append(',')
                .append(csv(m.getReference())).append(',').append(csv(m.getReason())).append(',').append(csv(m.getPerformedBy())).append('\n'));
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @GetMapping("/audit-logs")
    public String auditLogs(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                            Model model) {
        LocalDateTime start = (from == null ? LocalDate.now().minusDays(30) : from).atStartOfDay();
        LocalDateTime end = (to == null ? LocalDate.now() : to).plusDays(1).atStartOfDay();
        List<AuditLog> logs = auditLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);
        model.addAttribute("logs", logs); model.addAttribute("from", start.toLocalDate()); model.addAttribute("to", end.minusDays(1).toLocalDate());
        return "audit-logs";
    }

    @GetMapping("/transactions")
    public String transactionHistory(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                     @RequestParam(required = false) PaymentMode paymentMode,
                                     @RequestParam(required = false) String search, Model model) {
        LocalDateTime start = (from == null ? LocalDate.now().minusDays(30) : from).atStartOfDay();
        LocalDateTime end = (to == null ? LocalDate.now() : to).plusDays(1).atStartOfDay();
        List<Invoice> transactions = invoiceRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end).stream()
                .filter(invoice -> paymentMode == null || invoice.getPaymentMode() == paymentMode)
                .filter(invoice -> search == null || search.isBlank() || invoice.getInvoiceNumber().toLowerCase().contains(search.toLowerCase())
                        || (invoice.getCustomer() != null && ((invoice.getCustomer().getFullName() != null && invoice.getCustomer().getFullName().toLowerCase().contains(search.toLowerCase()))
                        || (invoice.getCustomer().getPhoneNumber() != null && invoice.getCustomer().getPhoneNumber().contains(search)))))
                .toList();
        model.addAttribute("transactions", transactions);
        model.addAttribute("paymentModes", PaymentMode.values());
        model.addAttribute("selectedPaymentMode", paymentMode);
        model.addAttribute("search", search == null ? "" : search);
        model.addAttribute("from", start.toLocalDate()); model.addAttribute("to", end.minusDays(1).toLocalDate());
        model.addAttribute("transactionTotal", transactions.stream().map(Invoice::getGrandTotal).reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
        return "transactions";
    }

    @GetMapping(value = "/transactions/export", produces = "text/csv")
    @ResponseBody
    public byte[] exportTransactions() {
        StringBuilder csv = new StringBuilder("Date,Invoice,Customer,Phone,Payment,Status,Discount,Total,Cashier\n");
        invoiceRepository.findAllByOrderByCreatedAtDesc().forEach(invoice -> csv.append(csv(invoice.getCreatedAt())).append(',').append(csv(invoice.getInvoiceNumber())).append(',')
                .append(csv(invoice.getCustomer() == null ? "Walk-in Customer" : invoice.getCustomer().getFullName())).append(',')
                .append(csv(invoice.getCustomer() == null ? "" : invoice.getCustomer().getPhoneNumber())).append(',').append(csv(invoice.getPaymentMode())).append(',')
                .append(csv(invoice.getStatus())).append(',').append(csv(invoice.getDiscount())).append(',').append(csv(invoice.getGrandTotal())).append(',').append(csv(invoice.getCashierUsername())).append('\n'));
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @GetMapping(value = "/audit-logs/export", produces = "text/csv")
    @ResponseBody
    public byte[] exportAuditLogs() {
        StringBuilder csv = new StringBuilder("Date,Action,Entity,Entity ID,Details,User\n");
        auditLogRepository.findTop100ByOrderByCreatedAtDesc().forEach(log -> csv.append(csv(log.getCreatedAt())).append(',')
                .append(csv(log.getAction())).append(',').append(csv(log.getEntityType())).append(',').append(csv(log.getEntityId())).append(',')
                .append(csv(log.getDetails())).append(',').append(csv(log.getPerformedBy())).append('\n'));
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String csv(Object value) { return "\"" + String.valueOf(value == null ? "" : value).replace("\"", "\"\"") + "\""; }
}
