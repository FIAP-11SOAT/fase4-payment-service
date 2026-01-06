package com.fiap.soat11.payment.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fiap.soat11.payment.entity.Payment;
import com.fiap.soat11.payment.entity.PaymentStatusEnum;
import com.fiap.soat11.payment.helpers.client.mercadopago.MarcadoPagoClient;
import com.fiap.soat11.payment.helpers.client.mercadopago.schemas.CreatePaymentPayload;
import com.fiap.soat11.payment.helpers.client.mercadopago.schemas.CreatePaymentResponse;
import com.fiap.soat11.payment.repository.PaymentRepository;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final MarcadoPagoClient marcadoPagoClient;
    private final String webhookUrl;

    public PaymentService(
        PaymentRepository paymentRepository, 
        MarcadoPagoClient marcadoPagoClient,
        @Value("${fase4.payment.service.marcadopago.webhookUrl}") String webhookUrl
    ) {
        this.paymentRepository = paymentRepository;
        this.marcadoPagoClient = marcadoPagoClient;
        this.webhookUrl = webhookUrl;
    }

    public Payment createPayment(String orderID, Double amount, String customerName) {
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID().toString());
        payment.setOrderID(orderID);
        payment.setAmount(amount);
        payment.setStatus(PaymentStatusEnum.PENDING);
        payment.setCreatedAt(getCurrentTimestamp());
        payment.setUpdatedAt(getCurrentTimestamp());

        CreatePaymentPayload payload = CreatePaymentPayload.create(
            "Pedido " + orderID,
            payment.getId(),
            this.webhookUrl,
            amount
        );
        
        CreatePaymentResponse response = marcadoPagoClient.createPayment(payload);
        
        if (response != null) {
            payment.setIntegrationID(response.inStoreOrderId());
            payment.setIntegrationQrCode(response.qrData());
        }

        return paymentRepository.save(payment);
    }

    public Optional<Payment> getPaymentById(String id) {
        return paymentRepository.findById(id);
    }

    public Payment updatePaymentStatus(String paymentId, PaymentStatusEnum status) {
        Optional<Payment> existingPayment = paymentRepository.findById(paymentId);
        
        if (existingPayment.isPresent()) {
            Payment payment = existingPayment.get();
            payment.setStatus(status);
            payment.setUpdatedAt(getCurrentTimestamp());
            return paymentRepository.update(payment);
        }
        
        throw new RuntimeException("Payment not found with id: " + paymentId);
    }

    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
