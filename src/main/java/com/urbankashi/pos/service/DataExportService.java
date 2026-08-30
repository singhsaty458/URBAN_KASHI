package com.urbankashi.pos.service;

import com.urbankashi.pos.model.*;
import com.urbankashi.pos.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DataExportService {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final InvoiceRepository invoiceRepository;
    private final StockMovementRepository stockMovementRepository;

    public ExportFile export(String tab, String format, LocalDateTime from, LocalDateTime to) {
        TabData data = dataFor(tab, from, to);
        return "xlsx".equalsIgnoreCase(format) ? new ExportFile(toExcel(data), data.fileStem() + ".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                : new ExportFile(toCsv(data), data.fileStem() + ".csv", "text/csv;charset=UTF-8");
    }

    private TabData dataFor(String tab, LocalDateTime from, LocalDateTime to) {
        return switch (tab) {
            case "inventory" -> inventory();
            case "customers" -> customers(from, to);
            case "transactions" -> transactions(from, to);
            case "stock" -> stock(from, to);
            default -> throw new IllegalArgumentException("Select a valid data tab");
        };
    }

    private TabData inventory() {
        List<List<Object>> rows = new ArrayList<>();
        productRepository.findAllWithVariants().forEach(product -> product.getVariants().forEach(variant -> rows.add(List.of(
                product.getName(), value(product.getBrand()), value(product.getCategory()), value(variant.getSku()), value(variant.getBarcode()),
                value(variant.getSize()), value(variant.getColor()), value(variant.getCostPrice()), value(variant.getSellingPrice()), value(variant.getStockQuantity())))));
        return new TabData("Inventory", "urban-kashi-inventory", List.of("Product", "Brand", "Category", "SKU", "Barcode", "Size", "Color", "Cost Price", "Selling Price", "Stock"), rows);
    }

    private TabData customers(LocalDateTime from, LocalDateTime to) {
        List<List<Object>> rows = customerRepository.findAll().stream().filter(customer -> inRange(customer.getCreatedAt(), from, to)).map(customer -> List.of(
                customer.getFullName(), customer.getPhoneNumber(), value(customer.getAddress()), value(customer.getLoyaltyPoints()), value(customer.getCreditBalance()), format(customer.getCreatedAt()))).toList();
        return new TabData("Customers", "urban-kashi-customers", List.of("Name", "Phone", "Address", "Loyalty Points", "Credit Balance", "Member Since"), rows);
    }

    private TabData transactions(LocalDateTime from, LocalDateTime to) {
        List<List<Object>> rows = invoiceRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(from, to).stream().map(invoice -> List.of(format(invoice.getCreatedAt()), invoice.getInvoiceNumber(),
                invoice.getCustomer() == null ? "Walk-in Customer" : value(invoice.getCustomer().getFullName()), invoice.getCustomer() == null ? "" : value(invoice.getCustomer().getPhoneNumber()),
                value(invoice.getPaymentMode()), value(invoice.getStatus()), value(invoice.getDiscount()), value(invoice.getGrandTotal()), value(invoice.getCashierUsername()))).toList();
        return new TabData("Transactions", "urban-kashi-transactions", List.of("Date", "Invoice", "Customer", "Phone", "Payment", "Status", "Discount", "Total", "Cashier"), rows);
    }

    private TabData stock(LocalDateTime from, LocalDateTime to) {
        List<List<Object>> rows = stockMovementRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(from, to).stream().map(movement -> List.of(format(movement.getCreatedAt()), value(movement.getType()),
                movement.getVariant().getProduct().getName(), value(movement.getVariant().getSku()), value(movement.getVariant().getBarcode()), value(movement.getVariant().getSize()), value(movement.getVariant().getColor()),
                value(movement.getQuantityChange()), value(movement.getQuantityAfter()), value(movement.getReference()), value(movement.getReason()), value(movement.getPerformedBy()))).toList();
        return new TabData("Stock Movements", "urban-kashi-stock-history", List.of("Date", "Type", "Product", "SKU", "Barcode", "Size", "Color", "Change", "Stock After", "Reference", "Reason", "User"), rows);
    }

    private byte[] toCsv(TabData data) {
        StringBuilder csv = new StringBuilder("\uFEFF"); appendCsvRow(csv, data.headers()); data.rows().forEach(row -> appendCsvRow(csv, row)); return csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
    private void appendCsvRow(StringBuilder csv, List<?> row) { csv.append(row.stream().map(this::escapeCsv).collect(java.util.stream.Collectors.joining(","))).append('\n'); }
    private String escapeCsv(Object value) { return "\"" + String.valueOf(value(value)).replace("\"", "\"\"") + "\""; }

    private byte[] toExcel(TabData data) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(data.sheetName()); CellStyle header = workbook.createCellStyle(); Font font = workbook.createFont(); font.setBold(true); header.setFont(font); header.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex()); header.setFillPattern(FillPatternType.SOLID_FOREGROUND); Font white = workbook.createFont(); white.setColor(IndexedColors.WHITE.getIndex()); white.setBold(true); header.setFont(white);
            Row headings = sheet.createRow(0); for (int i = 0; i < data.headers().size(); i++) { Cell cell = headings.createCell(i); cell.setCellValue(data.headers().get(i)); cell.setCellStyle(header); }
            for (int rowIndex = 0; rowIndex < data.rows().size(); rowIndex++) { Row row = sheet.createRow(rowIndex + 1); List<Object> values = data.rows().get(rowIndex); for (int column = 0; column < values.size(); column++) row.createCell(column).setCellValue(String.valueOf(value(values.get(column)))); }
            sheet.createFreezePane(0, 1); for (int i = 0; i < data.headers().size(); i++) sheet.autoSizeColumn(i); workbook.write(out); return out.toByteArray();
        } catch (Exception exception) { throw new IllegalStateException("Unable to create Excel export", exception); }
    }
    private boolean inRange(LocalDateTime value, LocalDateTime from, LocalDateTime to) { return value != null && !value.isBefore(from) && value.isBefore(to); }
    private String format(LocalDateTime value) { return value == null ? "" : DATE.format(value); }
    private Object value(Object value) { return value == null ? "" : value; }
    private record TabData(String sheetName, String fileStem, List<String> headers, List<List<Object>> rows) {}
    public record ExportFile(byte[] bytes, String fileName, String contentType) {}
}
