package com.fiap.soat11.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SendOrderPayloadPayment(
    @JsonProperty("order_id")
    String orderID
) {}
