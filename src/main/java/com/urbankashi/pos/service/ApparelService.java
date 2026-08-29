package com.urbankashi.pos.service;

import com.urbankashi.pos.model.Product;
import com.urbankashi.pos.model.ProductVariant;
import com.urbankashi.pos.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ApparelService {
    private static final List<String> STANDARD_SIZES = List.of("XS", "S", "M", "L", "XL", "XXL", "3XL");
    private final ProductRepository productRepository;

    public Product getProduct(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Product not found"));
    }

    public List<String> orderedSizes(Product product) {
        return product.getVariants().stream().map(ProductVariant::getSize).filter(Objects::nonNull).distinct()
                .sorted(Comparator.comparingInt(this::sizeRank)).toList();
    }

    public Map<String, Map<String, ProductVariant>> matrix(Product product) {
        Map<String, Map<String, ProductVariant>> matrix = new TreeMap<>();
        for (ProductVariant variant : product.getVariants()) {
            matrix.computeIfAbsent(variant.getColor(), ignored -> new HashMap<>()).put(variant.getSize(), variant);
        }
        return matrix;
    }

    public List<BrokenSizeRow> brokenSizeSets() {
        List<BrokenSizeRow> rows = new ArrayList<>();
        for (Product product : productRepository.findAll()) {
            Map<String, Map<String, ProductVariant>> matrix = matrix(product);
            for (Map.Entry<String, Map<String, ProductVariant>> entry : matrix.entrySet()) {
                List<String> sizes = entry.getValue().keySet().stream().sorted(Comparator.comparingInt(this::sizeRank)).toList();
                for (int i = 1; i < sizes.size() - 1; i++) {
                    ProductVariant current = entry.getValue().get(sizes.get(i));
                    ProductVariant lower = entry.getValue().get(sizes.get(i - 1));
                    ProductVariant upper = entry.getValue().get(sizes.get(i + 1));
                    if (current.getStockQuantity() <= 0 && lower.getStockQuantity() > 0 && upper.getStockQuantity() > 0) {
                        rows.add(new BrokenSizeRow(product.getId(), product.getName(), entry.getKey(), current.getSize()));
                    }
                }
            }
        }
        return rows;
    }

    public String generateSku(Product product, String size, String color) {
        String productCode = product.getName().replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        productCode = productCode.substring(0, Math.min(6, productCode.length()));
        return String.join("-", "UK", productCode, normalize(size), normalize(color));
    }

    private String normalize(String value) { return value == null ? "NA" : value.replaceAll("[^A-Za-z0-9]", "").toUpperCase(); }
    private int sizeRank(String size) {
        int standard = STANDARD_SIZES.indexOf(size.toUpperCase());
        if (standard >= 0) return standard;
        try { return 100 + Integer.parseInt(size); } catch (NumberFormatException ignored) { return 1000 + size.hashCode(); }
    }

    public record BrokenSizeRow(Long productId, String productName, String color, String missingSize) {}
}
