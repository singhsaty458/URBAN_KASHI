package com.urbankashi.pos.service;

import com.urbankashi.pos.model.Customer;
import com.urbankashi.pos.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final com.urbankashi.pos.repository.InvoiceRepository invoiceRepository;

    public Customer findOrCreate(String phoneNumber, String name) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return null; // Walk-in customer
        }
        return customerRepository.findByPhoneNumber(phoneNumber).orElseGet(() -> {
            Customer customer = new Customer();
            customer.setPhoneNumber(phoneNumber);
            customer.setFullName(name == null || name.trim().isEmpty() ? "Walk-in Customer" : name);
            customer.setLoyaltyPoints(0);
            customer.setCreditBalance(BigDecimal.ZERO);
            return customerRepository.save(customer);
        });
    }

    public void addLoyaltyPoints(Long customerId, BigDecimal grandTotal) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        int points = grandTotal.divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN).intValue();
        customer.setLoyaltyPoints(customer.getLoyaltyPoints() + points);
        customerRepository.save(customer);
    }

    public void addCredit(Long customerId, BigDecimal amount) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        if (customer.getCreditBalance() == null) {
            customer.setCreditBalance(BigDecimal.ZERO);
        }
        customer.setCreditBalance(customer.getCreditBalance().add(amount));
        customerRepository.save(customer);
    }

    public void deductCredit(Long customerId, BigDecimal amount) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        if (customer.getCreditBalance() == null) {
            customer.setCreditBalance(BigDecimal.ZERO);
        }
        customer.setCreditBalance(customer.getCreditBalance().subtract(amount));
        customerRepository.save(customer);
    }

    public List<Customer> searchCustomers(String query) {
        return customerRepository.searchCustomers(query);
    }

    public Optional<Customer> findByPhone(String phone) {
        return customerRepository.findByPhoneNumber(phone);
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }
    
    public Customer save(Customer customer) {
        return customerRepository.save(customer);
    }
    
    public Customer findById(Long id) {
        return customerRepository.findById(id).orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    @org.springframework.transaction.annotation.Transactional
    public void delete(Long id) {
        invoiceRepository.detachCustomer(id);
        customerRepository.deleteById(id);
    }
}
