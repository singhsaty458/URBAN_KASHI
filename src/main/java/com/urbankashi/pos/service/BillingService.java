package com.urbankashi.pos.service;

import com.urbankashi.pos.dto.CartItemDTO;
import com.urbankashi.pos.dto.InvoiceResponseDTO;
import com.urbankashi.pos.exception.InsufficientStockException;
import com.urbankashi.pos.model.*;
import com.urbankashi.pos.repository.InvoiceRepository;
import com.urbankashi.pos.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BillingService {

    private final ProductVariantRepository productVariantRepository;
    private final InvoiceRepository invoiceRepository;
    private final InventoryService inventoryService;
    private final CustomerService customerService;
    private final InvoiceNumberGenerator invoiceNumberGenerator;

    @Transactional(rollbackFor = Exception.class)
    public InvoiceResponseDTO generateInvoice(List<CartItemDTO> cartItems, String customerPhone, String customerName, PaymentMode paymentMode, BigDecimal discount) {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart cannot be empty");
        }

        Customer customer = customerService.findOrCreate(customerPhone, customerName);

        BigDecimal totalTaxable = BigDecimal.ZERO;
        BigDecimal totalCgst = BigDecimal.ZERO;
        BigDecimal totalSgst = BigDecimal.ZERO;
        BigDecimal grandTotal = BigDecimal.ZERO;

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(invoiceNumberGenerator.generateNext());
        invoice.setCustomer(customer);
        invoice.setPaymentMode(paymentMode);
        invoice.setDiscount(discount != null ? discount : BigDecimal.ZERO);

        List<InvoiceItem> items = new ArrayList<>();
        BigDecimal sumLineTotals = BigDecimal.ZERO;

        for (CartItemDTO dto : cartItems) {
            ProductVariant variant = productVariantRepository.findById(dto.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Variant not found: " + dto.getVariantId()));
            Product product = variant.getProduct();

            inventoryService.deductStock(variant.getId(), dto.getQuantity());

            BigDecimal sellingPrice = variant.getSellingPrice();
            BigDecimal lineTotal = sellingPrice.multiply(BigDecimal.valueOf(dto.getQuantity()));
            BigDecimal gstRate = product.getGstRate();
            
            BigDecimal divisor = BigDecimal.ONE.add(gstRate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
            BigDecimal taxableBase = lineTotal.divide(divisor, 2, RoundingMode.HALF_UP);
            
            BigDecimal totalGst = lineTotal.subtract(taxableBase);
            BigDecimal cgst = totalGst.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
            BigDecimal sgst = totalGst.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);

            InvoiceItem item = new InvoiceItem();
            item.setInvoice(invoice);
            item.setVariant(variant);
            item.setProductName(product.getName());
            item.setSize(variant.getSize());
            item.setColor(variant.getColor());
            item.setQuantity(dto.getQuantity());
            item.setUnitPrice(sellingPrice);
            item.setTaxableAmount(taxableBase);
            item.setCgstAmount(cgst);
            item.setSgstAmount(sgst);
            item.setTotalAmount(lineTotal);

            items.add(item);

            totalTaxable = totalTaxable.add(taxableBase);
            totalCgst = totalCgst.add(cgst);
            totalSgst = totalSgst.add(sgst);
            sumLineTotals = sumLineTotals.add(lineTotal);
        }

        grandTotal = sumLineTotals.subtract(invoice.getDiscount());

        invoice.setTotalTaxable(totalTaxable);
        invoice.setTotalCgst(totalCgst);
        invoice.setTotalSgst(totalSgst);
        invoice.setGrandTotal(grandTotal);
        invoice.setItems(items);

        Invoice savedInvoice = invoiceRepository.save(invoice);

        if (customer != null) {
            customerService.addLoyaltyPoints(customer.getId(), grandTotal);
        }

        return buildDto(savedInvoice);
    }

    public InvoiceResponseDTO getInvoiceByNumber(String invoiceNumber) {
        Invoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        return buildDto(invoice);
    }

    public InvoiceResponseDTO getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        return buildDto(invoice);
    }

    private InvoiceResponseDTO buildDto(Invoice invoice) {
        InvoiceResponseDTO dto = new InvoiceResponseDTO();
        dto.setInvoiceId(invoice.getId());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setCreatedAt(invoice.getCreatedAt() != null ? invoice.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "");
        dto.setTotalTaxable(invoice.getTotalTaxable());
        dto.setTotalCgst(invoice.getTotalCgst());
        dto.setTotalSgst(invoice.getTotalSgst());
        dto.setGrandTotal(invoice.getGrandTotal());
        dto.setDiscount(invoice.getDiscount());
        dto.setPaymentMode(invoice.getPaymentMode() != null ? invoice.getPaymentMode().name() : "");
        if (invoice.getCustomer() != null) {
            dto.setCustomerName(invoice.getCustomer().getFullName());
            dto.setCustomerPhone(invoice.getCustomer().getPhoneNumber());
        } else {
            dto.setCustomerName("Walk-in Customer");
            dto.setCustomerPhone("");
        }

        if (invoice.getItems() != null) {
            List<InvoiceResponseDTO.ItemDetail> details = invoice.getItems().stream().map(item -> {
                InvoiceResponseDTO.ItemDetail detail = new InvoiceResponseDTO.ItemDetail();
                detail.setProductName(item.getProductName());
                detail.setSize(item.getSize());
                detail.setColor(item.getColor());
                detail.setQuantity(item.getQuantity());
                detail.setUnitPrice(item.getUnitPrice());
                detail.setTaxableAmount(item.getTaxableAmount());
                detail.setCgst(item.getCgstAmount());
                detail.setSgst(item.getSgstAmount());
                detail.setTotal(item.getTotalAmount());
                return detail;
            }).collect(Collectors.toList());
            dto.setItems(details);
        }
        return dto;
    }
}
