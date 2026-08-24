package com.urbankashi.pos.service;

import com.urbankashi.pos.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;

@Service
@RequiredArgsConstructor
public class InvoiceNumberGenerator {

    private final InvoiceRepository invoiceRepository;

    public synchronized String generateNext() {
        int year = Year.now().getValue();
        LocalDateTime startOfYear = LocalDate.of(year, 1, 1).atStartOfDay();
        long count = invoiceRepository.countInvoicesSince(startOfYear);
        long sequence = count + 1;
        return String.format("UK-%d-%05d", year, sequence);
    }
}
