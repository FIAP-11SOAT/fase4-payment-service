package com.fiap.soat11.payment.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ConsumerPayloadOrder(
        UUID id,
        Double amount,
        @JsonProperty("customer_name")
        String customerName) {
}
