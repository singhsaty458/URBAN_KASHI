package com.urbankashi.pos.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import jakarta.servlet.http.HttpServletRequest;

import java.security.Principal;

@ControllerAdvice
public class CurrentUserModelAdvice {
    @ModelAttribute("currentUser")
    public String currentUser(Principal principal) {
        return principal == null ? "User" : principal.getName();
    }

    @ModelAttribute("sectionName")
    public String sectionName(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path.equals("/") || path.isBlank()) return "Dashboard";
        if (path.startsWith("/pos")) return "Billing / POS";
        if (path.startsWith("/inventory")) return "Inventory";
        if (path.startsWith("/customers")) return "Customers";
        if (path.startsWith("/returns")) return "Returns / Refunds";
        if (path.startsWith("/stock-history")) return "Stock History";
        if (path.startsWith("/audit-logs")) return "Audit Logs";
        if (path.startsWith("/account")) return "Account";
        return "Urban Kashi POS";
    }
}