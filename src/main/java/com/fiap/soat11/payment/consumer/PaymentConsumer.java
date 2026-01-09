package com.fiap.soat11.payment.consumer;

import org.springframework.stereotype.Service;

import com.fiap.soat11.payment.dto.ConsumerData;

import io.awspring.cloud.sqs.annotation.SqsListener;

@Service
public class PaymentConsumer {

    private final PaymentConsumerService paymentConsumerService;

    public PaymentConsumer(PaymentConsumerService paymentConsumerService) {
        this.paymentConsumerService = paymentConsumerService;
    }

    @SqsListener("fase4-payment-service-queue")
    public void listen(ConsumerData data) {
        paymentConsumerService.handler(data);
    }

}
