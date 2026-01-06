package com.fiap.soat11.payment.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class HealthController {

    @GetMapping
    public String status() {
        return "Payment Service is running";
    }

    @GetMapping("/health")
    public String health() {
        return "Payment Service is healthy";
    }

}
