package com.adrian.sintaxis.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Sintaxis Phone Store API is running! " +
                "To see available products, visit: /api/celulares or /api/accesorios. " +
                "For documentation, visit: /swagger-ui/index.html";
    }
}