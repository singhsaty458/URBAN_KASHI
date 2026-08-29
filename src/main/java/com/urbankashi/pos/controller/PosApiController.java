package com.urbankashi.pos.controller;

import com.urbankashi.pos.dto.CartItemDTO;
import com.urbankashi.pos.dto.InvoiceResponseDTO;
import com.urbankashi.pos.dto.ScanResultDTO;
import com.urbankashi.pos.dto.ReturnResponseDTO;
import com.urbankashi.pos.exception.InsufficientStockException;
import com.urbankashi.pos.model.Customer;
import com.urbankashi.pos.model.PaymentMode;
import com.urbankashi.pos.model.Product;
import com.urbankashi.pos.model.ProductVariant;
import com.urbankashi.pos.repository.ProductRepository;
import com.urbankashi.pos.repository.ProductVariantRepository;
import com.urbankashi.pos.service.BillingService;
import com.urbankashi.pos.service.CustomerService;
import com.urbankashi.pos.service.ReturnService;
import com.urbankashi.pos.service.WhatsAppInvoiceService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class PosApiController {

    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;
    private final BillingService billingService;
    private final CustomerService customerService;
    private final ReturnService returnService;
    private final WhatsAppInvoiceService whatsAppInvoiceService;

    @GetMapping("/scan")
    public ResponseEntity<ScanResultDTO> scanBarcode(@RequestParam String barcode) {
        Optional<ProductVariant> variantOpt = productVariantRepository.findByBarcode(barcode);
        if (variantOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        ProductVariant variant = variantOpt.get();
        Product product = variant.getProduct();
        
        ScanResultDTO dto = new ScanResultDTO();
        dto.setVariantId(variant.getId());
        dto.setProductName(product.getName());
        dto.setBrand(product.getBrand());
        dto.setGstRate(product.getGstRate() != null ? product.getGstRate() : java.math.BigDecimal.ZERO);
        dto.setHsnCode(product.getHsnCode());
        dto.setSize(variant.getSize());
        dto.setColor(variant.getColor());
        dto.setBarcode(variant.getBarcode());
        dto.setSellingPrice(variant.getSellingPrice());
        dto.setStockQuantity(variant.getStockQuantity());
        dto.setImageUrl(product.getImageUrl());
        
        return ResponseEntity.ok(dto);
    }

    @Data
    public static class CheckoutRequest {
        @NotEmpty
        private List<CartItemDTO> items;
        private String customerPhone;
        private String customerName;
        @NotBlank
        private String paymentMode;
        private BigDecimal discount = BigDecimal.ZERO;
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> processCheckout(@Valid @RequestBody CheckoutRequest request) {
        try {
            PaymentMode mode = PaymentMode.valueOf(request.getPaymentMode().toUpperCase());
            InvoiceResponseDTO response = billingService.generateInvoice(
                    request.getItems(), 
                    request.getCustomerPhone(), 
                    request.getCustomerName(), 
                    mode, 
                    request.getDiscount());
            return ResponseEntity.ok(response);
        } catch (InsufficientStockException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            log.error("Checkout failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/products/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String q) {
        return ResponseEntity.ok(productRepository.searchProducts(q));
    }

    @GetMapping("/customer")
    public ResponseEntity<Customer> lookupCustomer(@RequestParam String phone) {
        Optional<Customer> customer = customerService.findByPhone(phone);
        return customer.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/invoice/{invoiceNumber}")
    public ResponseEntity<InvoiceResponseDTO> getInvoice(@PathVariable String invoiceNumber) {
        return ResponseEntity.ok(billingService.getInvoiceByNumber(invoiceNumber));
    }

    @PostMapping("/returns")
    public ResponseEntity<ReturnResponseDTO> processReturn(
            @Valid @RequestBody com.urbankashi.pos.dto.ReturnRequestDTO request) {
        return ResponseEntity.ok(returnService.processReturn(request));
    }

    @PostMapping("/invoice/{invoiceId}/whatsapp")
    public ResponseEntity<?> sendInvoiceOnWhatsApp(@PathVariable Long invoiceId, @RequestParam String phone) {
        if (!whatsAppInvoiceService.isConfigured()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(java.util.Map.of("message", "WhatsApp Cloud API is not configured; using device sharing instead"));
        }
        whatsAppInvoiceService.sendInvoice(invoiceId, phone);
        return ResponseEntity.ok(java.util.Map.of("message", "PDF receipt sent on WhatsApp"));
    }

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productRepository.findAll());
    }
}

