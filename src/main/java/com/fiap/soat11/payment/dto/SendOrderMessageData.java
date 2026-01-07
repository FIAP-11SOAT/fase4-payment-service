package com.fiap.soat11.payment.dto;

public record SendOrderMessageData(
    ConsumerMeta meta,
    SendOrderPayload payload
) {}
