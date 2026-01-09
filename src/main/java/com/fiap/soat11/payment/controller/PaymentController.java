package com.fiap.soat11.payment.controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fiap.soat11.payment.dto.WebhookPayloadRequest;
import com.fiap.soat11.payment.entity.Payment;
import com.fiap.soat11.payment.service.PaymentService;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable String id) {
        Optional<Payment> payment = paymentService.getPaymentById(id);
        
        if (payment.isPresent()) {
            return ResponseEntity.ok(payment.get());
        }
        
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody WebhookPayloadRequest webhookPayload) {
        if (webhookPayload == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payload inválido");
        }

        System.out.println("Recebido webhook: " + webhookPayload);

        if (webhookPayload.topic() == null || webhookPayload.topic().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tópico do webhook ausente");
        }

        if (webhookPayload.topic().equals("payment")) {
            String paymentId = webhookPayload.resource();
            this.paymentService.updatePaymentWithWebHook(paymentId);
        } else {
            System.out.println("Tópico de webhook não tratado: " + webhookPayload.topic());
        }

        return ResponseEntity.ok("Webhook recebido com sucesso");
    }
}
