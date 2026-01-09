package com.fiap.soat11.payment.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.soat11.payment.dto.ConsumerMeta;
import com.fiap.soat11.payment.dto.SendOrderMessageData;
import com.fiap.soat11.payment.dto.SendOrderPayload;
import com.fiap.soat11.payment.dto.SendOrderPayloadPayment;
import com.fiap.soat11.payment.entity.Payment;
import com.fiap.soat11.payment.entity.PaymentStatusEnum;
import com.fiap.soat11.payment.helpers.client.mercadopago.MarcadoPagoClient;
import com.fiap.soat11.payment.helpers.client.mercadopago.schemas.CreatePaymentPayload;
import com.fiap.soat11.payment.helpers.client.mercadopago.schemas.CreatePaymentResponse;
import com.fiap.soat11.payment.helpers.client.mercadopago.schemas.GetPaymentByIdResponse;
import com.fiap.soat11.payment.repository.PaymentRepository;

import io.awspring.cloud.sqs.operations.SqsTemplate;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final MarcadoPagoClient marcadoPagoClient;
    private final String webhookUrl;
    private final SqsTemplate sqsTemplate;
    private final ObjectMapper objectMapper;

    public PaymentService(
            PaymentRepository paymentRepository,
            MarcadoPagoClient marcadoPagoClient,
            @Value("${fase4.payment.service.marcadopago.webhookUrl}") String webhookUrl,
            SqsTemplate sqsTemplate,
            ObjectMapper objectMapper) {
        this.paymentRepository = paymentRepository;
        this.marcadoPagoClient = marcadoPagoClient;
        this.webhookUrl = webhookUrl;
        this.sqsTemplate = sqsTemplate;
        this.objectMapper = objectMapper;
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
                "Pedido " + customerName,
                payment.getId(),
                this.webhookUrl,
                amount);

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

    public void updatePaymentWithWebHook(String paymentId) {
        GetPaymentByIdResponse response = marcadoPagoClient.getPaymentById(paymentId);

        if (response == null) {
            logger.error("Failed to fetch payment info from Mercado Pago for id: {}", paymentId);
            throw new RuntimeException("Failed to fetch payment info from Mercado Pago for id: " + paymentId);
        }

        logger.info("Fetched payment info from Mercado Pago: {}", response);

        PaymentStatusEnum status = PaymentStatusEnum.FAILED;
        String eventName = "payment-failed-event";

        if (response.status().equals("approved")) {
            status = PaymentStatusEnum.COMPLETED;
            eventName = "payment-completed-event";
        }

        Payment payment = this.paymentRepository.findById(response.externalReference()).orElseThrow(() -> {
            logger.error("Payment not found with id: {}", paymentId);
            return new RuntimeException("Payment not found with id: " + paymentId);
        });

        this.updatePaymentStatus(
                response.externalReference(),
                status);

        SendOrderMessageData messageData = new SendOrderMessageData(
                ConsumerMeta.create(
                        getCurrentTimestamp(),
                        "payment-service",
                        "order-service",
                        eventName),
                new SendOrderPayload(
                        new SendOrderPayloadPayment(
                                payment.getOrderID(),
                                payment.getId()
                             )));

        try {
            // Serializar para JSON antes de enviar
            String messageJson = objectMapper.writeValueAsString(messageData);
            sqsTemplate.send("fase4-order-service-queue", messageJson);
        } catch (Exception e) {
            logger.error("Erro ao enviar mensagem para SQS: {}", e.getMessage());
            throw new RuntimeException("Erro ao enviar mensagem para SQS", e);
        }
    }

    public Payment updatePaymentStatus(String paymentId, PaymentStatusEnum status) {
        Optional<Payment> existingPayment = paymentRepository.findById(paymentId);

        if (!existingPayment.isPresent()) {
            logger.error("Payment not found with id: {}", paymentId);
            throw new RuntimeException("Payment not found with id: " + paymentId);
        }

        Payment payment = existingPayment.get();
        payment.setStatus(status);
        payment.setUpdatedAt(getCurrentTimestamp());
        return paymentRepository.update(payment);

    }

    public String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
