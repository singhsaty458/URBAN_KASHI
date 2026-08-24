package com.urbankashi.pos.service;

import com.urbankashi.pos.exception.InsufficientStockException;
import com.urbankashi.pos.model.ProductVariant;
import com.urbankashi.pos.repository.ProductVariantRepository;
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

    @Transactional
    public void deductStock(Long variantId, int quantity) {
        ProductVariant variant = productVariantRepository.findById(variantId)
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
    }

    @Transactional
    public void addStock(Long variantId, int quantity) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Variant not found"));
        variant.setStockQuantity(variant.getStockQuantity() + quantity);
        productVariantRepository.save(variant);
    }

    public List<ProductVariant> getLowStockItems(int threshold) {
        return productVariantRepository.findLowStock(threshold);
    }
    
    public List<ProductVariant> getLowStockItems() {
        return getLowStockItems(5);
    }
}
