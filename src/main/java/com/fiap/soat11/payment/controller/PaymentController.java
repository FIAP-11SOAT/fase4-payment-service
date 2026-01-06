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
        try {
            if (webhookPayload == null) {
                return ResponseEntity.badRequest().body("Invalid webhook payload");
            }

            // Aqui você deve implementar a lógica específica baseada no tipo de webhook
            // Por exemplo, se for uma atualização de status de pagamento:
            // String integrationId = webhookPayload.getData().getId();
            // PaymentStatusEnum newStatus = determineStatusFromWebhook(webhookPayload);
            // paymentService.updatePaymentStatusByIntegrationId(integrationId, newStatus);
            
            System.out.println("Webhook recebido: " + webhookPayload);
            
            return ResponseEntity.ok("Webhook processed successfully");
            
        } catch (Exception e) {
            System.err.println("Erro ao processar webhook: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error processing webhook: " + e.getMessage());
        }
    }
}
