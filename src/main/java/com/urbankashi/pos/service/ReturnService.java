package com.urbankashi.pos.service;

import com.urbankashi.pos.dto.ReturnItemRequestDTO;
import com.urbankashi.pos.dto.ReturnRequestDTO;
import com.urbankashi.pos.dto.ReturnResponseDTO;
import com.urbankashi.pos.model.*;
import com.urbankashi.pos.repository.InvoiceRepository;
import com.urbankashi.pos.repository.ReturnItemRepository;
import com.urbankashi.pos.repository.ReturnTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Year;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReturnService {
    private final InvoiceRepository invoiceRepository;
    private final ReturnItemRepository returnItemRepository;
    private final ReturnTransactionRepository returnTransactionRepository;
    private final InventoryService inventoryService;
    private final CustomerService customerService;
    private final AuditService auditService;

    @Transactional(rollbackFor = Exception.class)
    public ReturnResponseDTO processReturn(ReturnRequestDTO request) {
        Invoice invoice = invoiceRepository.findByInvoiceNumber(request.getInvoiceNumber())
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + request.getInvoiceNumber()));
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("At least one item is required for return");
        }

        PaymentMode refundMode = parseRefundMode(request.getRefundMode(), invoice.getPaymentMode());
        Set<Long> requestedItemIds = new HashSet<>();
        ReturnTransaction transaction = ReturnTransaction.builder()
                .invoice(invoice)
                .returnNumber("RET-" + Year.now().getValue() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .refundMode(refundMode)
                .reason(request.getReason())
                .status("COMPLETED")
                .refundAmount(BigDecimal.ZERO)
                .build();

        BigDecimal refundTotal = BigDecimal.ZERO;
        for (ReturnItemRequestDTO requestItem : request.getItems()) {
            if (!requestedItemIds.add(requestItem.getInvoiceItemId())) {
                throw new IllegalArgumentException("Duplicate invoice item in return request");
            }
            InvoiceItem invoiceItem = invoice.getItems().stream()
                    .filter(item -> item.getId().equals(requestItem.getInvoiceItemId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invoice item does not belong to this invoice"));

            int alreadyReturned = returnItemRepository.sumReturnedQuantity(invoiceItem.getId());
            int availableToReturn = invoiceItem.getQuantity() - alreadyReturned;
            if (requestItem.getQuantity() > availableToReturn) {
                throw new IllegalArgumentException("Return quantity exceeds available quantity for " + invoiceItem.getProductName());
            }
            if (invoiceItem.getVariant() == null) {
                throw new IllegalArgumentException("Product variant is no longer available for stock return");
            }

            BigDecimal unitRefund = invoiceItem.getTotalAmount()
                    .divide(BigDecimal.valueOf(invoiceItem.getQuantity()), 2, RoundingMode.HALF_UP);
            BigDecimal itemRefund = unitRefund.multiply(BigDecimal.valueOf(requestItem.getQuantity()));
            ReturnItem returnItem = ReturnItem.builder()
                    .returnTransaction(transaction)
                    .invoiceItem(invoiceItem)
                    .quantity(requestItem.getQuantity())
                    .refundAmount(itemRefund)
                    .build();
            transaction.getItems().add(returnItem);
            refundTotal = refundTotal.add(itemRefund);
                inventoryService.addStock(invoiceItem.getVariant().getId(), requestItem.getQuantity(), StockMovementType.RETURN,
                    transaction.getReturnNumber(), "Customer return");
        }

        transaction.setRefundAmount(refundTotal);
        ReturnTransaction saved = returnTransactionRepository.save(transaction);
        BigDecimal totalRefunded = returnTransactionRepository.sumRefundAmountByInvoiceId(invoice.getId());
        invoice.setStatus(totalRefunded.compareTo(invoice.getGrandTotal()) >= 0 ? "FULLY_RETURNED" : "PARTIALLY_RETURNED");
        invoiceRepository.save(invoice);
        auditService.record("RETURN_COMPLETED", "ReturnTransaction", saved.getId(),
            "Return " + saved.getReturnNumber() + " for " + saved.getRefundAmount());
        if (invoice.getCustomer() != null) {
            customerService.addLoyaltyPoints(invoice.getCustomer().getId(), refundTotal.negate());
        }
        return ReturnResponseDTO.builder()
                .returnId(saved.getId())
                .returnNumber(saved.getReturnNumber())
                .invoiceNumber(invoice.getInvoiceNumber())
                .refundAmount(saved.getRefundAmount())
                .refundMode(saved.getRefundMode().name())
                .status(saved.getStatus())
                .build();
    }

    private PaymentMode parseRefundMode(String requestedMode, PaymentMode originalMode) {
        if (requestedMode == null || requestedMode.isBlank()) {
            return originalMode == null ? PaymentMode.CASH : originalMode;
        }
        try {
            return PaymentMode.valueOf(requestedMode.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unsupported refund mode: " + requestedMode);
        }
    }
}
