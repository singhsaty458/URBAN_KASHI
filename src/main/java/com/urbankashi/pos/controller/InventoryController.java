package com.urbankashi.pos.controller;

import com.urbankashi.pos.model.Product;
import com.urbankashi.pos.model.ProductVariant;
import com.urbankashi.pos.repository.ProductRepository;
import com.urbankashi.pos.repository.ProductVariantRepository;
import com.urbankashi.pos.service.InventoryService;
import com.urbankashi.pos.service.ImageStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.List;

@Controller
@RequestMapping("/inventory")
public class InventoryController {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final InventoryService inventoryService;
    private final com.urbankashi.pos.repository.InvoiceItemRepository invoiceItemRepository;
    private final ImageStorageService imageStorageService;
    private final com.urbankashi.pos.service.ApparelService apparelService;

    @Autowired
    public InventoryController(ProductRepository productRepository, 
                               ProductVariantRepository productVariantRepository,
                               InventoryService inventoryService,
                               com.urbankashi.pos.repository.InvoiceItemRepository invoiceItemRepository,
                               ImageStorageService imageStorageService,
                               com.urbankashi.pos.service.ApparelService apparelService) {
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
        this.inventoryService = inventoryService;
        this.invoiceItemRepository = invoiceItemRepository;
        this.imageStorageService = imageStorageService;
        this.apparelService = apparelService;
    }

    @GetMapping("")
    public String listInventory(Model model) {
        model.addAttribute("products", productRepository.findAll());
        return "inventory";
    }

    @PostMapping("/product")
    public String saveProduct(@ModelAttribute Product product, @RequestParam(value = "images", required = false) MultipartFile[] images) {
        if (product.getId() != null) {
            Product existing = productRepository.findById(product.getId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            existing.setName(product.getName());
            existing.setHsnCode(product.getHsnCode());
            existing.setGstRate(product.getGstRate());
            existing.setBrand(product.getBrand());
            existing.setCategory(product.getCategory());
            handleImageUploads(existing, images);
            productRepository.save(existing);
        } else {
            handleImageUploads(product, images);
            productRepository.save(product);
        }
        return "redirect:/inventory";
    }

    private void handleImageUploads(Product product, MultipartFile[] images) {
        if (images == null || images.length == 0 || (images.length == 1 && images[0].isEmpty())) {
            return;
        }

        for (MultipartFile file : images) {
            if (file.isEmpty()) continue;
            try {
                String fileUrl = imageStorageService.uploadImage(file);
                product.getImageUrls().add(fileUrl);
                if (product.getImageUrl() == null || product.getImageUrl().trim().isEmpty()) {
                    product.setImageUrl(fileUrl);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @PostMapping("/product/delete/{id}")
    @org.springframework.transaction.annotation.Transactional
    public String deleteProduct(@PathVariable Long id) {
        invoiceItemRepository.detachProductVariants(id);
        productRepository.deleteById(id);
        return "redirect:/inventory";
    }

    @PostMapping("/variant")
    public String saveVariant(@ModelAttribute ProductVariant variant, @RequestParam Long productId,
                              @RequestParam List<String> sizes, @RequestParam List<String> colors,
                              @RequestParam(value = "imageFiles", required = false) MultipartFile[] imageFiles) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (sizes.isEmpty() || colors.isEmpty()) {
            throw new IllegalArgumentException("Select at least one size and one color");
        }

        if (variant.getId() != null) {
            if (sizes.size() != 1 || colors.size() != 1) {
                throw new IllegalArgumentException("Select one size and one color while editing a variant");
            }
            ProductVariant targetVariant = productVariantRepository.findById(variant.getId())
                    .orElseThrow(() -> new RuntimeException("Variant not found"));
            targetVariant.setBarcode(variant.getBarcode());
            targetVariant.setSize(sizes.get(0));
            targetVariant.setColor(colors.get(0));
            if (targetVariant.getSku() == null) targetVariant.setSku(apparelService.generateSku(product, sizes.get(0), colors.get(0)));
            targetVariant.setCostPrice(variant.getCostPrice());
            targetVariant.setSellingPrice(variant.getSellingPrice());
            targetVariant.setStockQuantity(variant.getStockQuantity());
            uploadVariantImages(targetVariant, imageFiles);
            productVariantRepository.save(targetVariant);
        } else {
            boolean multiple = sizes.size() * colors.size() > 1;
            for (String size : sizes) {
                for (String color : colors) {
                    ProductVariant targetVariant = ProductVariant.builder()
                            .product(product).barcode(buildVariantBarcode(variant.getBarcode(), size, color, multiple))
                            .sku(apparelService.generateSku(product, size, color))
                            .size(size.trim()).color(color.trim()).costPrice(variant.getCostPrice())
                            .sellingPrice(variant.getSellingPrice()).stockQuantity(variant.getStockQuantity()).build();
                    uploadVariantImages(targetVariant, imageFiles);
                    productVariantRepository.save(targetVariant);
                }
            }
        }
        return "redirect:/inventory";
    }

    private String buildVariantBarcode(String base, String size, String color, boolean multiple) {
        if (!multiple) return base;
        String suffix = (size + "-" + color).toUpperCase().replaceAll("[^A-Z0-9]+", "-");
        String prefix = base == null ? "UK" : base.trim();
        return (prefix + "-" + suffix).substring(0, Math.min(100, prefix.length() + suffix.length() + 1));
    }

    private void uploadVariantImages(ProductVariant targetVariant, MultipartFile[] imageFiles) {
        if (imageFiles != null) {
            for (MultipartFile file : imageFiles) {
                if (!file.isEmpty()) {
                    try {
                        String fileUrl = imageStorageService.uploadImage(file);
                        targetVariant.getImageUrls().add(fileUrl);
                        if (targetVariant.getImageUrl() == null) targetVariant.setImageUrl(fileUrl);
                    } catch (IOException e) {
                        throw new RuntimeException("Variant image upload failed", e);
                    }
                }
            }
        }
    }

    @PostMapping("/variant/delete/{id}")
    @org.springframework.transaction.annotation.Transactional
    public String deleteVariant(@PathVariable Long id) {
        invoiceItemRepository.detachVariant(id);
        productVariantRepository.deleteById(id);
        return "redirect:/inventory";
    }

    @PostMapping("/stock/update")
    public String updateStock(@RequestParam Long variantId, @RequestParam int newQuantity) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Variant not found"));
        variant.setStockQuantity(newQuantity);
        productVariantRepository.save(variant);
        return "redirect:/inventory";
    }

    @PostMapping("/variant/{id}/stock")
    public String addStockToVariant(@PathVariable Long id, @RequestParam int quantity) {
        inventoryService.addStock(id, quantity);
        return "redirect:/inventory";
    }

    @PostMapping("/api/product")
    @ResponseBody
    public Product saveProductApi(@RequestBody Product product) {
        return productRepository.save(product);
    }

    @PostMapping("/api/variant")
    @ResponseBody
    public ProductVariant saveVariantApi(@RequestBody ProductVariant variant) {
        return productVariantRepository.save(variant);
    }
}
