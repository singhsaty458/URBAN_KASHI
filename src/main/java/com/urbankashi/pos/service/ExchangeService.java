package com.urbankashi.pos.service;

import com.urbankashi.pos.dto.*;
import com.urbankashi.pos.model.*;
import com.urbankashi.pos.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExchangeService {
    private final InvoiceRepository invoiceRepository;
    private final ReturnItemRepository returnItemRepository;
    private final ExchangeItemRepository exchangeItemRepository;
    private final ExchangeTransactionRepository exchangeTransactionRepository;
    private final ProductVariantRepository variantRepository;
    private final InventoryService inventoryService;
    private final BillingService billingService;
    private final CustomerService customerService;
    private final AuditService auditService;

    @Transactional(rollbackFor = Exception.class)
    public ExchangeResponseDTO replace(ExchangeRequestDTO request) {
        Invoice original = invoiceRepository.findByInvoiceNumber(request.getInvoiceNumber())
                .orElseThrow(() -> new IllegalArgumentException("Original invoice not found"));
        Map<Long, InvoiceItem> originalItems = new HashMap<>();
        original.getItems().forEach(item -> originalItems.put(item.getId(), item));
        Set<Long> uniqueLines = new HashSet<>();
        BigDecimal lineTotal = original.getItems().stream().map(InvoiceItem::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal invoiceRatio = lineTotal.signum() == 0 ? BigDecimal.ONE : original.getGrandTotal().divide(lineTotal, 8, RoundingMode.HALF_UP);
        BigDecimal credit = BigDecimal.ZERO;
        List<ExchangeItem> exchangeItems = new ArrayList<>();

        for (ReturnItemRequestDTO requested : request.getReturnedItems()) {
            if (!uniqueLines.add(requested.getInvoiceItemId())) throw new IllegalArgumentException("Duplicate replacement line selected");
            InvoiceItem sold = originalItems.get(requested.getInvoiceItemId());
            if (sold == null) throw new IllegalArgumentException("Selected item does not belong to this invoice");
            int alreadyProcessed = returnItemRepository.sumReturnedQuantity(sold.getId()) + exchangeItemRepository.sumExchangedQuantity(sold.getId());
            if (requested.getQuantity() <= 0 || requested.getQuantity() > sold.getQuantity() - alreadyProcessed) {
                throw new IllegalArgumentException("Replacement quantity exceeds eligible quantity for " + sold.getProductName());
            }
            if (sold.getVariant() == null) throw new IllegalArgumentException("Original product variant is no longer available");
            BigDecimal unitValue = sold.getTotalAmount().divide(BigDecimal.valueOf(sold.getQuantity()), 8, RoundingMode.HALF_UP).multiply(invoiceRatio);
            BigDecimal itemCredit = unitValue.multiply(BigDecimal.valueOf(requested.getQuantity())).setScale(2, RoundingMode.HALF_UP);
            credit = credit.add(itemCredit);
            exchangeItems.add(ExchangeItem.builder().originalInvoiceItem(sold).quantity(requested.getQuantity()).creditAmount(itemCredit).build());
        }

        BigDecimal replacementValue = BigDecimal.ZERO;
        for (CartItemDTO item : request.getReplacementItems()) {
            if (item.getVariantId() == null || item.getQuantity() == null || item.getQuantity() <= 0) throw new IllegalArgumentException("Invalid replacement item");
            ProductVariant variant = variantRepository.findByIdForUpdate(item.getVariantId()).orElseThrow(() -> new IllegalArgumentException("Replacement variant not found"));
            replacementValue = replacementValue.add(variant.getSellingPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        if (replacementValue.compareTo(credit) < 0) {
            throw new IllegalArgumentException("Replacement products must be equal to or above the returned value. No cash refund is allowed.");
        }

        String exchangeNumber = "EX-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        for (ExchangeItem item : exchangeItems) {
            inventoryService.addStock(item.getOriginalInvoiceItem().getVariant().getId(), item.getQuantity(), StockMovementType.EXCHANGE, exchangeNumber, "Product replacement received");
        }
        Customer customer = original.getCustomer();
        PaymentMode paymentMode;
        try { paymentMode = PaymentMode.valueOf(request.getPaymentMode().toUpperCase()); }
        catch (Exception exception) { throw new IllegalArgumentException("Select a valid payment mode for the price difference"); }
        InvoiceResponseDTO replacementInvoice = billingService.generateInvoice(request.getReplacementItems(),
                customer == null ? null : customer.getPhoneNumber(), customer == null ? null : customer.getFullName(), paymentMode, credit);
        Invoice replacementEntity = invoiceRepository.findById(replacementInvoice.getInvoiceId()).orElseThrow();

        ExchangeTransaction transaction = ExchangeTransaction.builder().exchangeNumber(exchangeNumber).originalInvoice(original)
                .replacementInvoice(replacementEntity).replacementCredit(credit).amountPaid(replacementInvoice.getGrandTotal())
                .reason(request.getReason()).performedBy(currentUsername()).build();
        exchangeItems.forEach(item -> item.setExchangeTransaction(transaction));
        transaction.setItems(exchangeItems);
        ExchangeTransaction saved = exchangeTransactionRepository.save(transaction);

        boolean fullyProcessed = original.getItems().stream().allMatch(item ->
                returnItemRepository.sumReturnedQuantity(item.getId()) + exchangeItemRepository.sumExchangedQuantity(item.getId()) >= item.getQuantity());
        original.setStatus(fullyProcessed ? "FULLY_REPLACED" : "PARTIALLY_REPLACED");
        invoiceRepository.save(original);
        if (customer != null) customerService.addLoyaltyPoints(customer.getId(), credit.negate());
        auditService.record("PRODUCT_REPLACED", "ExchangeTransaction", saved.getId(), exchangeNumber + "; credit " + credit + "; paid " + replacementInvoice.getGrandTotal());
        return ExchangeResponseDTO.builder().exchangeId(saved.getId()).exchangeNumber(exchangeNumber)
                .originalInvoiceNumber(original.getInvoiceNumber()).replacementCredit(credit)
                .amountPaid(replacementInvoice.getGrandTotal()).replacementInvoice(replacementInvoice).build();
    }

    private String currentUsername() {
        return SecurityContextHolder.getContext().getAuthentication() == null ? "system" : SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
