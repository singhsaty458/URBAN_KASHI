package com.urbankashi.pos.service;

import com.urbankashi.pos.dto.*;
import com.urbankashi.pos.model.*;
import com.urbankashi.pos.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeServiceTest {
    @Mock InvoiceRepository invoiceRepository; @Mock ReturnItemRepository returnItemRepository;
    @Mock ExchangeItemRepository exchangeItemRepository; @Mock ExchangeTransactionRepository exchangeTransactionRepository;
    @Mock ProductVariantRepository variantRepository; @Mock InventoryService inventoryService; @Mock BillingService billingService;
    @Mock CustomerService customerService; @Mock AuditService auditService;

    @Test
    void replacesProductAndChargesOnlyDifference() {
        Product product = Product.builder().name("Shirt").build();
        ProductVariant oldVariant = ProductVariant.builder().id(1L).product(product).sellingPrice(new BigDecimal("1000")).build();
        ProductVariant newVariant = ProductVariant.builder().id(2L).product(product).sellingPrice(new BigDecimal("1200")).stockQuantity(5).build();
        InvoiceItem sold = InvoiceItem.builder().id(10L).variant(oldVariant).productName("Shirt").quantity(1).totalAmount(new BigDecimal("1000")).build();
        Invoice original = Invoice.builder().id(5L).invoiceNumber("UK-1").grandTotal(new BigDecimal("1000")).items(List.of(sold)).build();
        Invoice replacement = Invoice.builder().id(6L).invoiceNumber("UK-2").build();
        when(invoiceRepository.findByInvoiceNumber("UK-1")).thenReturn(Optional.of(original));
        when(returnItemRepository.sumReturnedQuantity(10L)).thenReturn(0);
        when(exchangeItemRepository.sumExchangedQuantity(10L)).thenReturn(0, 1);
        when(variantRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(newVariant));
        when(billingService.generateInvoice(anyList(), isNull(), isNull(), eq(PaymentMode.CASH), eq(new BigDecimal("1000.00"))))
                .thenReturn(InvoiceResponseDTO.builder().invoiceId(6L).grandTotal(new BigDecimal("200.00")).build());
        when(invoiceRepository.findById(6L)).thenReturn(Optional.of(replacement));
        when(exchangeTransactionRepository.save(any())).thenAnswer(call -> { ExchangeTransaction value=call.getArgument(0); value.setId(7L); return value; });
        ExchangeRequestDTO request = new ExchangeRequestDTO(); request.setInvoiceNumber("UK-1"); request.setPaymentMode("CASH");
        ReturnItemRequestDTO returned = new ReturnItemRequestDTO(); returned.setInvoiceItemId(10L); returned.setQuantity(1);
        CartItemDTO replacementItem = new CartItemDTO(); replacementItem.setVariantId(2L); replacementItem.setQuantity(1);
        request.setReturnedItems(List.of(returned)); request.setReplacementItems(List.of(replacementItem));

        ExchangeResponseDTO response = service().replace(request);

        assertEquals(new BigDecimal("1000.00"), response.getReplacementCredit());
        assertEquals(new BigDecimal("200.00"), response.getAmountPaid());
        assertEquals("FULLY_REPLACED", original.getStatus());
        verify(inventoryService).addStock(eq(1L), eq(1), eq(StockMovementType.EXCHANGE), anyString(), anyString());
    }

    @Test
    void rejectsReplacementBelowReturnedValue() {
        Product product=Product.builder().name("Shirt").build();
        ProductVariant oldVariant=ProductVariant.builder().id(1L).product(product).build();
        ProductVariant cheaper=ProductVariant.builder().id(2L).product(product).sellingPrice(new BigDecimal("900")).build();
        InvoiceItem sold=InvoiceItem.builder().id(10L).variant(oldVariant).productName("Shirt").quantity(1).totalAmount(new BigDecimal("1000")).build();
        Invoice invoice=Invoice.builder().id(5L).invoiceNumber("UK-1").grandTotal(new BigDecimal("1000")).items(List.of(sold)).build();
        when(invoiceRepository.findByInvoiceNumber("UK-1")).thenReturn(Optional.of(invoice)); when(returnItemRepository.sumReturnedQuantity(10L)).thenReturn(0);
        when(exchangeItemRepository.sumExchangedQuantity(10L)).thenReturn(0); when(variantRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(cheaper));
        ExchangeRequestDTO request=new ExchangeRequestDTO();request.setInvoiceNumber("UK-1");request.setPaymentMode("CASH");
        ReturnItemRequestDTO returned=new ReturnItemRequestDTO();returned.setInvoiceItemId(10L);returned.setQuantity(1); CartItemDTO replacement=new CartItemDTO();replacement.setVariantId(2L);replacement.setQuantity(1);
        request.setReturnedItems(List.of(returned));request.setReplacementItems(List.of(replacement));
        assertThrows(IllegalArgumentException.class,()->service().replace(request));
        verifyNoInteractions(inventoryService,billingService);
    }

    private ExchangeService service() { return new ExchangeService(invoiceRepository,returnItemRepository,exchangeItemRepository,exchangeTransactionRepository,variantRepository,inventoryService,billingService,customerService,auditService); }
}
