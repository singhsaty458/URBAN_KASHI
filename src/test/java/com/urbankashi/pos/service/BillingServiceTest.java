package com.urbankashi.pos.service;

import com.urbankashi.pos.dto.CartItemDTO;
import com.urbankashi.pos.exception.InsufficientStockException;
import com.urbankashi.pos.model.PaymentMode;
import com.urbankashi.pos.repository.InvoiceRepository;
import com.urbankashi.pos.repository.ProductVariantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillingServiceTest {
    @Mock ProductVariantRepository variantRepository;
    @Mock InvoiceRepository invoiceRepository;
    @Mock InventoryService inventoryService;
    @Mock CustomerService customerService;
    @Mock InvoiceNumberGenerator numberGenerator;
    @Mock AuditService auditService;
    private BillingService billingService;

    @BeforeEach
    void setUp() {
        billingService = new BillingService(variantRepository, invoiceRepository, inventoryService, customerService, numberGenerator, auditService);
    }

    @Test
    void rejectsEmptyCartBeforeChangingData() {
        assertThrows(IllegalArgumentException.class, () -> billingService.generateInvoice(List.of(), null, null, PaymentMode.CASH, BigDecimal.ZERO));
        verifyNoInteractions(variantRepository, invoiceRepository, inventoryService);
    }

    @Test
    void rejectsNonPositiveQuantityBeforeChangingStock() {
        CartItemDTO item = new CartItemDTO(10L, 0);
        assertThrows(IllegalArgumentException.class, () -> billingService.generateInvoice(List.of(item), null, null, PaymentMode.CASH, BigDecimal.ZERO));
        verifyNoInteractions(variantRepository, inventoryService);
    }

    @Test
    void rejectsNegativeDiscount() {
        assertThrows(IllegalArgumentException.class, () -> billingService.generateInvoice(
                List.of(new CartItemDTO(10L, 1)), null, null, PaymentMode.CASH, BigDecimal.ONE.negate()));
        verifyNoInteractions(variantRepository, inventoryService);
    }
}
