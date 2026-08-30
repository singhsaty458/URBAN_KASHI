package com.urbankashi.pos.service;

import com.urbankashi.pos.dto.InvoiceResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class WhatsAppInvoiceService {
    private final BillingService billingService;
    private final PdfGenerationService pdfGenerationService;

    @Value("${app.whatsapp.access-token:}") private String accessToken;
    @Value("${app.whatsapp.phone-number-id:}") private String phoneNumberId;
    @Value("${app.whatsapp.graph-version:v22.0}") private String graphVersion;
    @Value("${app.whatsapp.receipt-template:}") private String receiptTemplate;
    @Value("${app.whatsapp.template-language:en_US}") private String templateLanguage;

    public boolean isConfigured() {
        return accessToken != null && !accessToken.isBlank() && phoneNumberId != null && !phoneNumberId.isBlank();
    }

    public void sendInvoice(Long invoiceId, String customerPhone) {
        if (!isConfigured()) throw new IllegalStateException("WhatsApp Business Cloud API is not configured");
        InvoiceResponseDTO invoice = billingService.getInvoiceById(invoiceId);
        byte[] pdf = pdfGenerationService.generateInvoicePdf(invoice);
        String phone = normalizePhone(customerPhone);
        String baseUrl = "https://graph.facebook.com/" + graphVersion + "/" + phoneNumberId;
        RestClient client = RestClient.builder().defaultHeader("Authorization", "Bearer " + accessToken).build();

        MultipartBodyBuilder multipart = new MultipartBodyBuilder();
        multipart.part("messaging_product", "whatsapp");
        multipart.part("type", "application/pdf");
        multipart.part("file", new ByteArrayResource(pdf) {
            @Override public String getFilename() { return invoice.getInvoiceNumber() + ".pdf"; }
        }).contentType(MediaType.APPLICATION_PDF);

        @SuppressWarnings("unchecked")
        Map<String, Object> upload = client.post().uri(baseUrl + "/media")
                .contentType(MediaType.MULTIPART_FORM_DATA).body(multipart.build()).retrieve().body(Map.class);
        if (upload == null || upload.get("id") == null) throw new IllegalStateException("WhatsApp media upload failed");

        Map<String, Object> document = Map.of("id", upload.get("id"), "filename", invoice.getInvoiceNumber() + ".pdf",
            "caption", "Urban Kashi receipt " + invoice.getInvoiceNumber() + " • Total ₹" + invoice.getGrandTotal());
        Map<String, Object> request = receiptTemplate == null || receiptTemplate.isBlank()
            ? Map.of("messaging_product", "whatsapp", "recipient_type", "individual", "to", phone, "type", "document", "document", document)
            : templateRequest(phone, document);
        client.post().uri(baseUrl + "/messages").contentType(MediaType.APPLICATION_JSON)
                .body(request).retrieve().toBodilessEntity();
    }

        private Map<String, Object> templateRequest(String phone, Map<String, Object> document) {
        Map<String, Object> header = Map.of("type", "header", "parameters", java.util.List.of(
            Map.of("type", "document", "document", Map.of("id", document.get("id"), "filename", document.get("filename")))));
        Map<String, Object> template = Map.of("name", receiptTemplate, "language", Map.of("code", templateLanguage),
            "components", java.util.List.of(header));
        return Map.of("messaging_product", "whatsapp", "recipient_type", "individual", "to", phone,
            "type", "template", "template", template);
        }

    private String normalizePhone(String phone) {
        String digits = phone == null ? "" : phone.replaceAll("\\D", "");
        if (digits.length() == 10) digits = "91" + digits;
        if (digits.length() < 11 || digits.length() > 15) throw new IllegalArgumentException("Enter a valid WhatsApp phone number");
        return digits;
    }
}
