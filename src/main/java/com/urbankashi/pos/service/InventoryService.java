package com.urbankashi.pos.service;

import com.urbankashi.pos.exception.InsufficientStockException;
import com.urbankashi.pos.model.ProductVariant;
import com.urbankashi.pos.model.StockMovement;
import com.urbankashi.pos.model.StockMovementType;
import com.urbankashi.pos.repository.ProductVariantRepository;
import com.urbankashi.pos.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryService {

    private final ProductVariantRepository productVariantRepository;
    private final StockMovementRepository stockMovementRepository;
    private final AuditService auditService;

    @Transactional
    public void deductStock(Long variantId, int quantity) {
        deductStock(variantId, quantity, StockMovementType.SALE, null, "POS checkout");
    }

    @Transactional
    public void deductStock(Long variantId, int quantity, StockMovementType type, String reference, String reason) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Stock quantity must be greater than zero");
        }
        ProductVariant variant = productVariantRepository.findByIdForUpdate(variantId)
                .orElseThrow(() -> new RuntimeException("Variant not found"));
        
        if (variant.getStockQuantity() < quantity) {
            String msg = String.format("Insufficient stock for %s (%s/%s). Available: %d, Requested: %d",
                    variant.getProduct().getName(), variant.getSize(), variant.getColor(),
                    variant.getStockQuantity(), quantity);
            log.error(msg);
            throw new InsufficientStockException(msg);
        }
        
        variant.setStockQuantity(variant.getStockQuantity() - quantity);
        productVariantRepository.save(variant);
        recordMovement(variant, type, -quantity, reference, reason);
    }

    @Transactional
    public void addStock(Long variantId, int quantity) {
        addStock(variantId, quantity, StockMovementType.ADJUSTMENT, null, "Manual stock addition");
    }

    @Transactional
    public void addStock(Long variantId, int quantity, StockMovementType type, String reference, String reason) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Stock quantity must be greater than zero");
        }
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Variant not found"));
        variant.setStockQuantity(variant.getStockQuantity() + quantity);
        productVariantRepository.save(variant);
        recordMovement(variant, type, quantity, reference, reason);
    }

    private void recordMovement(ProductVariant variant, StockMovementType type, int quantityChange, String reference, String reason) {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication() == null
                ? "system" : org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        stockMovementRepository.save(StockMovement.builder().variant(variant).type(type).quantityChange(quantityChange)
                .quantityAfter(variant.getStockQuantity()).reference(reference).reason(reason).performedBy(username).build());
        auditService.record("STOCK_" + type, "ProductVariant", variant.getId(), "Quantity change: " + quantityChange + "; " + reason);
    }

    public List<ProductVariant> getLowStockItems(int threshold) {
        return productVariantRepository.findLowStock(threshold);
    }
    
    public List<ProductVariant> getLowStockItems() {
        return getLowStockItems(5);
    }
}
