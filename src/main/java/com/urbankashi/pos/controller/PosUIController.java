package com.urbankashi.pos.controller;

import com.urbankashi.pos.dto.InvoiceResponseDTO;
import com.urbankashi.pos.repository.CustomerRepository;
import com.urbankashi.pos.repository.ProductRepository;
import com.urbankashi.pos.service.BillingService;
import com.urbankashi.pos.service.PdfGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class PosUIController {

    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final BillingService billingService;
    private final PdfGenerationService pdfGenerationService;

    @GetMapping("/pos")
    public String pos(Model model) {
        model.addAttribute("products", productRepository.findAll()); 
        model.addAttribute("customers", customerRepository.findAll());
        return "pos-billing";
    }

    @GetMapping("/invoice/{id}")
    public String invoicePrint(@PathVariable Long id, Model model) {
        model.addAttribute("invoice", billingService.getInvoiceById(id));
        return "invoice-print";
    }

    @GetMapping(value = "/invoice/{id}/pdf", produces = "application/pdf")
    public @ResponseBody byte[] getInvoicePdf(@PathVariable Long id) {
        InvoiceResponseDTO invoice = billingService.getInvoiceById(id);
        return pdfGenerationService.generateInvoicePdf(invoice);
    }
}
