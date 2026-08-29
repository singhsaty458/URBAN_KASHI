package com.urbankashi.pos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReturnController {
    @GetMapping("/returns")
    public String returns() {
        return "returns";
    }
}
