package com.urbankashi.pos.service;

import com.urbankashi.pos.dto.ReturnItemRequestDTO;
import com.urbankashi.pos.dto.ReturnRequestDTO;
import com.urbankashi.pos.dto.ReturnResponseDTO;
import com.urbankashi.pos.model.*;
import com.urbankashi.pos.repository.InvoiceRepository;
import com.urbankashi.pos.repository.ReturnItemRepository;
import com.urbankashi.pos.repository.ReturnTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReturnServiceTest {
    @Mock InvoiceRepository invoiceRepository;
    @Mock ReturnItemRepository returnItemRepository;
    @Mock ReturnTransactionRepository returnTransactionRepository;
    @Mock InventoryService inventoryService;
    @Mock CustomerService customerService;
    @Mock AuditService auditService;

    @Test
    void returnsItemAndRestoresStock() {
        Product product = Product.builder().name("T-Shirt").build();
        ProductVariant variant = ProductVariant.builder().id(7L).product(product).build();
        InvoiceItem invoiceItem = InvoiceItem.builder().id(9L).variant(variant).productName("T-Shirt")
                .quantity(2).totalAmount(new BigDecimal("200.00")).build();
        Invoice invoice = Invoice.builder().id(3L).invoiceNumber("UK-2026-00001")
            .paymentMode(PaymentMode.UPI).grandTotal(new BigDecimal("200.00")).items(List.of(invoiceItem)).build();
        when(invoiceRepository.findByInvoiceNumber(invoice.getInvoiceNumber())).thenReturn(Optional.of(invoice));
        when(returnItemRepository.sumReturnedQuantity(9L)).thenReturn(0);
        when(returnTransactionRepository.save(any(ReturnTransaction.class))).thenAnswer(invocation -> {
            ReturnTransaction saved = invocation.getArgument(0);
            saved.setId(15L);
            return saved;
        });
        when(returnTransactionRepository.sumRefundAmountByInvoiceId(3L)).thenReturn(new BigDecimal("100.00"));

        ReturnItemRequestDTO item = new ReturnItemRequestDTO();
        item.setInvoiceItemId(9L); item.setQuantity(1);
        ReturnRequestDTO request = new ReturnRequestDTO();
        request.setInvoiceNumber(invoice.getInvoiceNumber()); request.setItems(List.of(item));

        ReturnResponseDTO result = new ReturnService(invoiceRepository, returnItemRepository, returnTransactionRepository, inventoryService, customerService, auditService)
                .processReturn(request);

        assertEquals(new BigDecimal("100.00"), result.getRefundAmount());
        assertEquals("UPI", result.getRefundMode());
        verify(inventoryService).addStock(eq(7L), eq(1), eq(StockMovementType.RETURN), anyString(), anyString());
    }

    @Test
    void rejectsReturningMoreThanSoldQuantity() {
        InvoiceItem item = InvoiceItem.builder().id(9L).quantity(1).productName("T-Shirt").build();
        Invoice invoice = Invoice.builder().invoiceNumber("UK-2026-00001").items(List.of(item)).build();
        when(invoiceRepository.findByInvoiceNumber(invoice.getInvoiceNumber())).thenReturn(Optional.of(invoice));
        when(returnItemRepository.sumReturnedQuantity(9L)).thenReturn(1);
        ReturnItemRequestDTO requestItem = new ReturnItemRequestDTO();
        requestItem.setInvoiceItemId(9L); requestItem.setQuantity(1);
        ReturnRequestDTO request = new ReturnRequestDTO();
        request.setInvoiceNumber(invoice.getInvoiceNumber()); request.setItems(List.of(requestItem));

        assertThrows(IllegalArgumentException.class, () -> new ReturnService(invoiceRepository, returnItemRepository,
            returnTransactionRepository, inventoryService, customerService, auditService).processReturn(request));
        verifyNoInteractions(inventoryService, returnTransactionRepository);
    }
}
