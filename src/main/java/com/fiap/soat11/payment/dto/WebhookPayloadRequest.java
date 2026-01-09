package com.fiap.soat11.payment.dto;

public record WebhookPayloadRequest(
    String resource,
    String topic
) {}
