package com.fiap.soat11.payment.helpers.client.mercadopago.schemas;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CreatePaymentResponse(
    @JsonProperty("in_store_order_id") 
    String inStoreOrderId,

    @JsonProperty("qr_data") 
    String qrData
) {}
