package com.urbankashi.pos.controller;

import com.urbankashi.pos.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final ReportService reportService;

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("dashboard", reportService.getDashboardData());
        return "dashboard";
    }
}
