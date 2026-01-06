package com.fiap.soat11.payment.helpers.client.mercadopago;

import com.fiap.soat11.payment.helpers.client.mercadopago.schemas.CreatePaymentPayload;
import com.fiap.soat11.payment.helpers.client.mercadopago.schemas.CreatePaymentResponse;

public interface MarcadoPagoClient {
    CreatePaymentResponse createPayment(CreatePaymentPayload payload);
}
