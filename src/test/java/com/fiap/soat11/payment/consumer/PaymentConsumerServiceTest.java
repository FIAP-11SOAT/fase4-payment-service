package com.fiap.soat11.payment.consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.soat11.payment.dto.ConsumerData;
import com.fiap.soat11.payment.dto.ConsumerMeta;
import com.fiap.soat11.payment.dto.ConsumerPayload;
import com.fiap.soat11.payment.dto.ConsumerPayloadOrder;
import com.fiap.soat11.payment.entity.Payment;
import com.fiap.soat11.payment.entity.PaymentStatusEnum;
import com.fiap.soat11.payment.service.PaymentService;

import io.awspring.cloud.sqs.operations.SqsTemplate;

@ExtendWith(MockitoExtension.class)
class PaymentConsumerServiceTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private SqsTemplate sqsTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PaymentConsumerService paymentConsumerService;

    private ConsumerMeta createTestMeta(String eventName) {
        return ConsumerMeta.create(
            "2026-01-09T10:00:00",
            "order-service",
            "payment-service",
            eventName
        );
    }

    @BeforeEach
    void setUp() {
        lenient().when(paymentService.getCurrentTimestamp()).thenReturn("2026-01-09T10:00:00");
    }

    @Test
    void testHandler_OrderPaymentRequested_Success() throws JsonProcessingException {
        // Arrange
        UUID orderId = UUID.randomUUID();
        ConsumerPayloadOrder order = new ConsumerPayloadOrder(
            orderId,
            100.0,
            "John Doe"
        );
        
        ConsumerPayload payload = new ConsumerPayload(order);
        ConsumerMeta meta = createTestMeta("order-payment-requested-event");
        ConsumerData data = new ConsumerData(meta, payload);

        Payment createdPayment = new Payment();
        createdPayment.setId("payment-123");
        createdPayment.setOrderID(orderId.toString());
        createdPayment.setAmount(100.0);
        createdPayment.setStatus(PaymentStatusEnum.PENDING);

        when(paymentService.createPayment(orderId.toString(), 100.0, "John Doe"))
            .thenReturn(createdPayment);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"test\":\"data\"}");

        // Act
        paymentConsumerService.handler(data);

        // Assert
        verify(paymentService, times(1)).createPayment(orderId.toString(), 100.0, "John Doe");
        verify(objectMapper, times(1)).writeValueAsString(any());
        verify(sqsTemplate, times(1)).send(eq("fase4-order-service-queue"), anyString());
    }

    @Test
    void testHandler_OrderPaymentRequested_WithNullCustomerName() throws JsonProcessingException {
        // Arrange
        UUID orderId = UUID.randomUUID();
        ConsumerPayloadOrder order = new ConsumerPayloadOrder(
            orderId,
            50.0,
            null  // customerName is null
        );
        
        ConsumerPayload payload = new ConsumerPayload(order);
        ConsumerMeta meta = createTestMeta("order-payment-requested-event");
        ConsumerData data = new ConsumerData(meta, payload);

        Payment createdPayment = new Payment();
        createdPayment.setId("payment-456");
        createdPayment.setOrderID(orderId.toString());
        createdPayment.setAmount(50.0);

        when(paymentService.createPayment(orderId.toString(), 50.0, "Anonymous User"))
            .thenReturn(createdPayment);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"test\":\"data\"}");

        // Act
        paymentConsumerService.handler(data);

        // Assert
        verify(paymentService, times(1)).createPayment(orderId.toString(), 50.0, "Anonymous User");
        verify(sqsTemplate, times(1)).send(eq("fase4-order-service-queue"), anyString());
    }

    @Test
    void testHandler_OrderPaymentRequested_WithNullOrder() {
        // Arrange
        ConsumerPayload payload = new ConsumerPayload(null);  // order is null
        ConsumerMeta meta = createTestMeta("order-payment-requested-event");
        ConsumerData data = new ConsumerData(meta, payload);

        // Act
        paymentConsumerService.handler(data);

        // Assert
        verify(paymentService, never()).createPayment(anyString(), anyDouble(), anyString());
        verify(sqsTemplate, never()).send(anyString(), anyString());
    }

    @Test
    void testHandler_OrderPaymentRequested_PaymentServiceException() throws JsonProcessingException {
        // Arrange
        UUID orderId = UUID.randomUUID();
        ConsumerPayloadOrder order = new ConsumerPayloadOrder(
            orderId,
            100.0,
            "John Doe"
        );
        
        ConsumerPayload payload = new ConsumerPayload(order);
        ConsumerMeta meta = createTestMeta("order-payment-requested-event");
        ConsumerData data = new ConsumerData(meta, payload);

        when(paymentService.createPayment(anyString(), anyDouble(), anyString()))
            .thenThrow(new RuntimeException("Payment creation failed"));

        // Act
        paymentConsumerService.handler(data);

        // Assert
        verify(paymentService, times(1)).createPayment(orderId.toString(), 100.0, "John Doe");
        verify(sqsTemplate, never()).send(anyString(), anyString());
    }

    @Test
    void testHandler_OrderPaymentRequested_JsonProcessingException() throws JsonProcessingException {
        // Arrange
        UUID orderId = UUID.randomUUID();
        ConsumerPayloadOrder order = new ConsumerPayloadOrder(
            orderId,
            100.0,
            "John Doe"
        );
        
        ConsumerPayload payload = new ConsumerPayload(order);
        ConsumerMeta meta = createTestMeta("order-payment-requested-event");
        ConsumerData data = new ConsumerData(meta, payload);

        Payment createdPayment = new Payment();
        createdPayment.setId("payment-789");
        createdPayment.setOrderID(orderId.toString());

        when(paymentService.createPayment(orderId.toString(), 100.0, "John Doe"))
            .thenReturn(createdPayment);
        when(objectMapper.writeValueAsString(any()))
            .thenThrow(new JsonProcessingException("JSON error") {});

        // Act
        paymentConsumerService.handler(data);

        // Assert
        verify(paymentService, times(1)).createPayment(orderId.toString(), 100.0, "John Doe");
        verify(objectMapper, times(1)).writeValueAsString(any());
        verify(sqsTemplate, never()).send(anyString(), anyString());
    }

    @Test
    void testHandler_OrderPaymentRequested_SqsException() throws JsonProcessingException {
        // Arrange
        UUID orderId = UUID.randomUUID();
        ConsumerPayloadOrder order = new ConsumerPayloadOrder(
            orderId,
            100.0,
            "John Doe"
        );
        
        ConsumerPayload payload = new ConsumerPayload(order);
        ConsumerMeta meta = createTestMeta("order-payment-requested-event");
        ConsumerData data = new ConsumerData(meta, payload);

        Payment createdPayment = new Payment();
        createdPayment.setId("payment-error");
        createdPayment.setOrderID(orderId.toString());

        when(paymentService.createPayment(orderId.toString(), 100.0, "John Doe"))
            .thenReturn(createdPayment);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"test\":\"data\"}");
        doThrow(new RuntimeException("SQS send failed"))
            .when(sqsTemplate).send(anyString(), anyString());

        // Act
        paymentConsumerService.handler(data);

        // Assert
        verify(paymentService, times(1)).createPayment(orderId.toString(), 100.0, "John Doe");
        verify(sqsTemplate, times(1)).send(eq("fase4-order-service-queue"), anyString());
    }

    @Test
    void testHandler_UnknownEvent() {
        // Arrange
        ConsumerPayload payload = new ConsumerPayload(null);
        ConsumerMeta meta = createTestMeta("unknown-event-type");
        ConsumerData data = new ConsumerData(meta, payload);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            paymentConsumerService.handler(data);
        });

        verify(paymentService, never()).createPayment(anyString(), anyDouble(), anyString());
        verify(sqsTemplate, never()).send(anyString(), anyString());
    }

    @Test
    void testHandleOrderPaymentRequested_DirectCall_Success() throws Exception {
        // Arrange
        UUID orderId = UUID.randomUUID();
        ConsumerPayloadOrder order = new ConsumerPayloadOrder(
            orderId,
            200.0,
            "Jane Doe"
        );
        
        ConsumerPayload payload = new ConsumerPayload(order);
        ConsumerMeta meta = createTestMeta("order-payment-requested-event");
        ConsumerData data = new ConsumerData(meta, payload);

        Payment createdPayment = new Payment();
        createdPayment.setId("payment-999");
        createdPayment.setOrderID(orderId.toString());
        createdPayment.setAmount(200.0);

        when(paymentService.createPayment(orderId.toString(), 200.0, "Jane Doe"))
            .thenReturn(createdPayment);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"payment\":\"data\"}");

        // Act
        paymentConsumerService.handleOrderPaymentRequested(data);

        // Assert
        verify(paymentService, times(1)).createPayment(orderId.toString(), 200.0, "Jane Doe");
        verify(paymentService, times(1)).getCurrentTimestamp();
        verify(objectMapper, times(1)).writeValueAsString(any());
        verify(sqsTemplate, times(1)).send(eq("fase4-order-service-queue"), eq("{\"payment\":\"data\"}"));
    }
}
