package com.fiap.soat11.payment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.soat11.payment.entity.Payment;
import com.fiap.soat11.payment.entity.PaymentStatusEnum;
import com.fiap.soat11.payment.helpers.client.mercadopago.MarcadoPagoClient;
import com.fiap.soat11.payment.helpers.client.mercadopago.schemas.CreatePaymentPayload;
import com.fiap.soat11.payment.helpers.client.mercadopago.schemas.CreatePaymentResponse;
import com.fiap.soat11.payment.helpers.client.mercadopago.schemas.GetPaymentByIdResponse;
import com.fiap.soat11.payment.repository.PaymentRepository;

import io.awspring.cloud.sqs.operations.SqsTemplate;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private MarcadoPagoClient marcadoPagoClient;

    @Mock
    private SqsTemplate sqsTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PaymentService paymentService;

    private String webhookUrl = "http://test-webhook.com";

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(
            paymentRepository,
            marcadoPagoClient,
            webhookUrl,
            sqsTemplate,
            objectMapper
        );
    }

    @Test
    void testCreatePayment_Success() {
        // Arrange
        String orderId = "order-123";
        Double amount = 100.0;
        String customerName = "John Doe";

        CreatePaymentResponse mpResponse = new CreatePaymentResponse(
            "mp-order-123",
            "qr-code-data-123"
        );

        Payment savedPayment = new Payment();
        savedPayment.setOrderID(orderId);
        savedPayment.setAmount(amount);
        savedPayment.setStatus(PaymentStatusEnum.PENDING);
        savedPayment.setIntegrationID("mp-order-123");
        savedPayment.setIntegrationQrCode("qr-code-data-123");

        when(marcadoPagoClient.createPayment(any(CreatePaymentPayload.class))).thenReturn(mpResponse);
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        // Act
        Payment result = paymentService.createPayment(orderId, amount, customerName);

        // Assert
        assertNotNull(result);
        assertEquals(orderId, result.getOrderID());
        assertEquals(amount, result.getAmount());
        assertEquals(PaymentStatusEnum.PENDING, result.getStatus());
        assertEquals("mp-order-123", result.getIntegrationID());
        assertEquals("qr-code-data-123", result.getIntegrationQrCode());

        verify(marcadoPagoClient, times(1)).createPayment(any(CreatePaymentPayload.class));
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void testCreatePayment_WithNullMercadoPagoResponse() {
        // Arrange
        String orderId = "order-456";
        Double amount = 50.0;
        String customerName = "Jane Doe";

        Payment savedPayment = new Payment();
        savedPayment.setOrderID(orderId);
        savedPayment.setAmount(amount);
        savedPayment.setStatus(PaymentStatusEnum.PENDING);

        when(marcadoPagoClient.createPayment(any(CreatePaymentPayload.class))).thenReturn(null);
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        // Act
        Payment result = paymentService.createPayment(orderId, amount, customerName);

        // Assert
        assertNotNull(result);
        assertEquals(orderId, result.getOrderID());
        assertNull(result.getIntegrationID());
        assertNull(result.getIntegrationQrCode());

        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void testGetPaymentById_Found() {
        // Arrange
        String paymentId = "payment-123";
        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setOrderID("order-123");

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // Act
        Optional<Payment> result = paymentService.getPaymentById(paymentId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(paymentId, result.get().getId());
        verify(paymentRepository, times(1)).findById(paymentId);
    }

    @Test
    void testGetPaymentById_NotFound() {
        // Arrange
        String paymentId = "nonexistent-payment";

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        // Act
        Optional<Payment> result = paymentService.getPaymentById(paymentId);

        // Assert
        assertFalse(result.isPresent());
        verify(paymentRepository, times(1)).findById(paymentId);
    }

    @Test
    void testUpdatePaymentStatus_Success() {
        // Arrange
        String paymentId = "payment-123";
        Payment existingPayment = new Payment();
        existingPayment.setId(paymentId);
        existingPayment.setStatus(PaymentStatusEnum.PENDING);

        Payment updatedPayment = new Payment();
        updatedPayment.setId(paymentId);
        updatedPayment.setStatus(PaymentStatusEnum.COMPLETED);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(existingPayment));
        when(paymentRepository.update(any(Payment.class))).thenReturn(updatedPayment);

        // Act
        Payment result = paymentService.updatePaymentStatus(paymentId, PaymentStatusEnum.COMPLETED);

        // Assert
        assertNotNull(result);
        assertEquals(PaymentStatusEnum.COMPLETED, result.getStatus());
        verify(paymentRepository, times(1)).findById(paymentId);
        verify(paymentRepository, times(1)).update(any(Payment.class));
    }

    @Test
    void testUpdatePaymentStatus_PaymentNotFound() {
        // Arrange
        String paymentId = "nonexistent-payment";

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.updatePaymentStatus(paymentId, PaymentStatusEnum.COMPLETED);
        });

        assertEquals("Payment not found with id: " + paymentId, exception.getMessage());
        verify(paymentRepository, times(1)).findById(paymentId);
        verify(paymentRepository, never()).update(any(Payment.class));
    }

    @Test
    void testUpdatePaymentWithWebHook_ApprovedStatus() throws JsonProcessingException {
        // Arrange
        String paymentId = "mp-payment-123";
        String externalReference = "payment-123";

        GetPaymentByIdResponse mpResponse = new GetPaymentByIdResponse(
            externalReference,
            "approved"
        );

        Payment existingPayment = new Payment();
        existingPayment.setId(externalReference);
        existingPayment.setOrderID("order-123");
        existingPayment.setStatus(PaymentStatusEnum.PENDING);

        Payment updatedPayment = new Payment();
        updatedPayment.setId(externalReference);
        updatedPayment.setOrderID("order-123");
        updatedPayment.setStatus(PaymentStatusEnum.COMPLETED);

        when(marcadoPagoClient.getPaymentById(paymentId)).thenReturn(mpResponse);
        when(paymentRepository.findById(externalReference)).thenReturn(Optional.of(existingPayment));
        when(paymentRepository.update(any(Payment.class))).thenReturn(updatedPayment);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"test\":\"data\"}");

        // Act
        paymentService.updatePaymentWithWebHook(paymentId);

        // Assert
        verify(marcadoPagoClient, times(1)).getPaymentById(paymentId);
        verify(paymentRepository, times(2)).findById(externalReference); // Called twice: in updatePaymentWithWebHook and updatePaymentStatus
        verify(paymentRepository, times(1)).update(any(Payment.class));
        verify(sqsTemplate, times(1)).send(eq("fase4-order-service-queue"), anyString());
    }

    @Test
    void testUpdatePaymentWithWebHook_FailedStatus() throws JsonProcessingException {
        // Arrange
        String paymentId = "mp-payment-456";
        String externalReference = "payment-456";

        GetPaymentByIdResponse mpResponse = new GetPaymentByIdResponse(
            externalReference,
            "rejected"
        );

        Payment existingPayment = new Payment();
        existingPayment.setId(externalReference);
        existingPayment.setOrderID("order-456");
        existingPayment.setStatus(PaymentStatusEnum.PENDING);

        Payment updatedPayment = new Payment();
        updatedPayment.setId(externalReference);
        updatedPayment.setOrderID("order-456");
        updatedPayment.setStatus(PaymentStatusEnum.FAILED);

        when(marcadoPagoClient.getPaymentById(paymentId)).thenReturn(mpResponse);
        when(paymentRepository.findById(externalReference)).thenReturn(Optional.of(existingPayment));
        when(paymentRepository.update(any(Payment.class))).thenReturn(updatedPayment);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"test\":\"data\"}");

        // Act
        paymentService.updatePaymentWithWebHook(paymentId);

        // Assert
        verify(marcadoPagoClient, times(1)).getPaymentById(paymentId);
        verify(paymentRepository, times(2)).findById(externalReference); // Called twice: in updatePaymentWithWebHook and updatePaymentStatus
        verify(paymentRepository, times(1)).update(any(Payment.class));
        verify(sqsTemplate, times(1)).send(eq("fase4-order-service-queue"), anyString());
    }

    @Test
    void testUpdatePaymentWithWebHook_MercadoPagoResponseNull() {
        // Arrange
        String paymentId = "mp-payment-789";

        when(marcadoPagoClient.getPaymentById(paymentId)).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.updatePaymentWithWebHook(paymentId);
        });

        assertEquals("Failed to fetch payment info from Mercado Pago for id: " + paymentId, exception.getMessage());
        verify(marcadoPagoClient, times(1)).getPaymentById(paymentId);
        verify(paymentRepository, never()).findById(anyString());
    }

    @Test
    void testUpdatePaymentWithWebHook_PaymentNotFound() {
        // Arrange
        String paymentId = "mp-payment-999";
        String externalReference = "nonexistent-payment";

        GetPaymentByIdResponse mpResponse = new GetPaymentByIdResponse(
            externalReference,
            "approved"
        );

        when(marcadoPagoClient.getPaymentById(paymentId)).thenReturn(mpResponse);
        when(paymentRepository.findById(externalReference)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.updatePaymentWithWebHook(paymentId);
        });

        assertEquals("Payment not found with id: " + paymentId, exception.getMessage());
        verify(marcadoPagoClient, times(1)).getPaymentById(paymentId);
        verify(paymentRepository, times(1)).findById(externalReference);
    }

    @Test
    void testUpdatePaymentWithWebHook_SqsException() throws JsonProcessingException {
        // Arrange
        String paymentId = "mp-payment-error";
        String externalReference = "payment-error";

        GetPaymentByIdResponse mpResponse = new GetPaymentByIdResponse(
            externalReference,
            "approved"
        );

        Payment existingPayment = new Payment();
        existingPayment.setId(externalReference);
        existingPayment.setOrderID("order-error");

        Payment updatedPayment = new Payment();
        updatedPayment.setId(externalReference);

        when(marcadoPagoClient.getPaymentById(paymentId)).thenReturn(mpResponse);
        when(paymentRepository.findById(externalReference)).thenReturn(Optional.of(existingPayment));
        when(paymentRepository.update(any(Payment.class))).thenReturn(updatedPayment);
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("JSON error") {});

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.updatePaymentWithWebHook(paymentId);
        });

        assertEquals("Erro ao enviar mensagem para SQS", exception.getMessage());
        verify(sqsTemplate, never()).send(anyString(), anyString());
    }

    @Test
    void testGetCurrentTimestamp() {
        // Act
        String timestamp = paymentService.getCurrentTimestamp();

        // Assert
        assertNotNull(timestamp);
        assertFalse(timestamp.isEmpty());
        // Verifica formato ISO_LOCAL_DATE_TIME (yyyy-MM-ddTHH:mm:ss)
        assertTrue(timestamp.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?"));
    }
}
