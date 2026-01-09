package com.fiap.soat11.payment.helpers.client.mercadopago.schemas;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GetPaymentByIdResponse(
    @JsonProperty("external_reference") 
    String externalReference,
    String status
) {}
