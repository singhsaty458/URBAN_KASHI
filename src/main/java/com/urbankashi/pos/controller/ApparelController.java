package com.urbankashi.pos.controller;

import com.urbankashi.pos.model.Product;
import com.urbankashi.pos.model.ProductVariant;
import com.urbankashi.pos.repository.ProductVariantRepository;
import com.urbankashi.pos.service.ApparelService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class ApparelController {
    private final ApparelService apparelService;
    private final ProductVariantRepository variantRepository;

    @GetMapping("/inventory/matrix/{productId}")
    public String matrix(@PathVariable Long productId, Model model) {
        Product product = apparelService.getProduct(productId);
        model.addAttribute("product", product);
        model.addAttribute("sizes", apparelService.orderedSizes(product));
        model.addAttribute("matrix", apparelService.matrix(product));
        return "stock-matrix";
    }

    @GetMapping("/apparel-reports")
    public String reports(Model model) {
        model.addAttribute("brokenSizes", apparelService.brokenSizeSets());
        return "apparel-reports";
    }

    @GetMapping("/inventory/variant/{variantId}/label")
    public String label(@PathVariable Long variantId, Model model) {
        ProductVariant variant = variantRepository.findById(variantId).orElseThrow(() -> new IllegalArgumentException("Variant not found"));
        model.addAttribute("variant", variant);
        return "barcode-label";
    }
}
