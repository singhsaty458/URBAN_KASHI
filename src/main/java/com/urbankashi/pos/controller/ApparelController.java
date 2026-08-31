package com.urbankashi.pos.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.urbankashi.pos.model.Product;
import com.urbankashi.pos.model.ProductVariant;
import com.urbankashi.pos.repository.ProductVariantRepository;
import com.urbankashi.pos.service.ApparelService;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import javax.imageio.ImageIO;
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
        model.addAttribute("qrCode", generateQrCode(variant));
        return "barcode-label";
    }

    private String generateQrCode(ProductVariant variant) {
        String payload = String.join("|",
                "UKPOS",
                variant.getBarcode(),
                variant.getProduct().getName(),
                variant.getSize(),
                variant.getColor(),
                variant.getSellingPrice().toPlainString());
        BitMatrix matrix;
        try {
            matrix = new QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, 180, 180);
        } catch (WriterException e) {
            throw new IllegalStateException("Unable to generate the product QR code", e);
        }

        BufferedImage image = new BufferedImage(matrix.getWidth(), matrix.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < matrix.getHeight(); y++) {
            for (int x = 0; x < matrix.getWidth(); x++) {
                image.setRGB(x, y, matrix.get(x, y) ? 0x000000 : 0xFFFFFF);
            }
        }

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            ImageIO.write(image, "PNG", output);
            return Base64.getEncoder().encodeToString(output.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create the product QR image", e);
        }
    }
}
