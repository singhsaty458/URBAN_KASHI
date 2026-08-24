package com.urbankashi.pos.controller;

import com.urbankashi.pos.model.Customer;
import com.urbankashi.pos.repository.InvoiceRepository;
import com.urbankashi.pos.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final InvoiceRepository invoiceRepository;

    @GetMapping("")
    public String listCustomers(Model model) {
        model.addAttribute("customers", customerService.getAllCustomers());
        return "customers";
    }

    @GetMapping("/{id}")
    public String customerDetail(@PathVariable Long id, Model model) {
        Customer customer = customerService.findById(id);
        model.addAttribute("customer", customer);
        model.addAttribute("invoices", invoiceRepository.findByCustomerId(id));
        return "customer-detail";
    }

    @PostMapping("")
    public String saveCustomer(@ModelAttribute Customer customer) {
        if (customer.getId() != null) {
            Customer existing = customerService.findById(customer.getId());
            existing.setFullName(customer.getFullName());
            existing.setPhoneNumber(customer.getPhoneNumber());
            customerService.save(existing);
        } else {
            customerService.findOrCreate(customer.getPhoneNumber(), customer.getFullName());
        }
        return "redirect:/customers";
    }

    @PostMapping("/{id}/credit")
    public String addCredit(@PathVariable Long id, @RequestParam BigDecimal amount) {
        customerService.addCredit(id, amount);
        return "redirect:/customers/" + id;
    }

    @PostMapping("/{id}/deduct-credit")
    public String deductCredit(@PathVariable Long id, @RequestParam BigDecimal amount) {
        customerService.deductCredit(id, amount);
        return "redirect:/customers/" + id;
    }

    @PostMapping("/delete/{id}")
    public String deleteCustomer(@PathVariable Long id) {
        customerService.delete(id);
        return "redirect:/customers";
    }
}
