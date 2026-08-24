package com.urbankashi.pos.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.urbankashi.pos.dto.InvoiceResponseDTO;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;

@Service
public class PdfGenerationService {

    public byte[] generateInvoicePdf(InvoiceResponseDTO invoice) {
        Document document = new Document(PageSize.A6, 10, 10, 10, 10);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        try {
            PdfWriter.getInstance(document, out);
            document.open();
            
            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Font.BOLD);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Font.BOLD);
            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 7, Font.NORMAL);
            
            // Header
            Paragraph brand = new Paragraph("URBAN KASHI", titleFont);
            brand.setAlignment(Element.ALIGN_CENTER);
            document.add(brand);
            
            Paragraph address = new Paragraph("Main Market, Varanasi, UP\nPhone: +91 9876543210\nGSTIN: 09XXXXXXXXXX1Z1", subtitleFont);
            address.setAlignment(Element.ALIGN_CENTER);
            document.add(address);
            
            document.add(new Paragraph("\n"));
            
            // Invoice Metadata
            Paragraph invNum = new Paragraph("Receipt: " + invoice.getInvoiceNumber(), boldFont);
            Paragraph date = new Paragraph("Date: " + invoice.getCreatedAt(), normalFont);
            document.add(invNum);
            document.add(date);
            
            if (invoice.getCustomerName() != null && !invoice.getCustomerName().trim().isEmpty()) {
                Paragraph customer = new Paragraph("Customer: " + invoice.getCustomerName(), normalFont);
                document.add(customer);
                if (invoice.getCustomerPhone() != null && !invoice.getCustomerPhone().trim().isEmpty()) {
                    Paragraph phone = new Paragraph("Phone: " + invoice.getCustomerPhone(), normalFont);
                    document.add(phone);
                }
            }
            
            document.add(new Paragraph("\n"));
            
            // Table
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{40f, 15f, 20f, 25f});
            
            // Headers
            PdfPCell h1 = new PdfPCell(new Paragraph("Item", boldFont));
            PdfPCell h2 = new PdfPCell(new Paragraph("Qty", boldFont));
            PdfPCell h3 = new PdfPCell(new Paragraph("Price", boldFont));
            PdfPCell h4 = new PdfPCell(new Paragraph("Total", boldFont));
            h4.setHorizontalAlignment(Element.ALIGN_RIGHT);
            
            setCellProperties(h1);
            setCellProperties(h2);
            setCellProperties(h3);
            setCellProperties(h4);
            
            table.addCell(h1);
            table.addCell(h2);
            table.addCell(h3);
            table.addCell(h4);
            
            if (invoice.getItems() != null) {
                for (InvoiceResponseDTO.ItemDetail item : invoice.getItems()) {
                    PdfPCell nameCell = new PdfPCell(new Paragraph(item.getProductName() + " (" + item.getSize() + "/" + item.getColor() + ")", normalFont));
                    PdfPCell qtyCell = new PdfPCell(new Paragraph(String.valueOf(item.getQuantity()), normalFont));
                    PdfPCell priceCell = new PdfPCell(new Paragraph(String.valueOf(item.getUnitPrice()), normalFont));
                    PdfPCell totalCell = new PdfPCell(new Paragraph(String.valueOf(item.getTotal()), normalFont));
                    totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    
                    setCellProperties(nameCell);
                    setCellProperties(qtyCell);
                    setCellProperties(priceCell);
                    setCellProperties(totalCell);
                    
                    table.addCell(nameCell);
                    table.addCell(qtyCell);
                    table.addCell(priceCell);
                    table.addCell(totalCell);
                }
            }
            
            document.add(table);
            document.add(new Paragraph("----------------------------------------------------------------------------------", smallFont));
            
            // Totals
            PdfPTable totalsTable = new PdfPTable(2);
            totalsTable.setWidthPercentage(100);
            totalsTable.setWidths(new float[]{60f, 40f});
            
            BigDecimal subtotal = invoice.getTotalTaxable().add(invoice.getTotalCgst()).add(invoice.getTotalSgst());
            if (invoice.getDiscount() != null) {
                subtotal = subtotal.add(invoice.getDiscount());
            }
            
            addTotalRow(totalsTable, "Subtotal", subtotal.toString(), normalFont);
            addTotalRow(totalsTable, "Taxable Amount", invoice.getTotalTaxable().toString(), normalFont);
            addTotalRow(totalsTable, "CGST", invoice.getTotalCgst().toString(), normalFont);
            addTotalRow(totalsTable, "SGST", invoice.getTotalSgst().toString(), normalFont);
            if (invoice.getDiscount() != null && invoice.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
                addTotalRow(totalsTable, "Discount", invoice.getDiscount().toString(), normalFont);
            }
            addTotalRow(totalsTable, "Grand Total", "Rs." + invoice.getGrandTotal().toString(), boldFont);
            addTotalRow(totalsTable, "Payment Mode", invoice.getPaymentMode(), normalFont);
            
            document.add(totalsTable);
            
            document.add(new Paragraph("\n"));
            Paragraph footer = new Paragraph("Thank you for shopping at Urban Kashi! \nVisit us again!", subtitleFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);
            
            document.close();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        
        return out.toByteArray();
    }
    
    private void setCellProperties(PdfPCell cell) {
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderWidth(0.5f);
        cell.setPadding(2);
    }
    
    private void addTotalRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell labelCell = new PdfPCell(new Paragraph(label, font));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(1);
        
        PdfPCell valueCell = new PdfPCell(new Paragraph(value, font));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setPadding(1);
        
        table.addCell(labelCell);
        table.addCell(valueCell);
    }
}

