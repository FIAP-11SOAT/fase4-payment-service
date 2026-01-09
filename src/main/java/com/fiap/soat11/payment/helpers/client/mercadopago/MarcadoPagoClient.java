package com.fiap.soat11.payment.helpers.client.mercadopago;

import com.fiap.soat11.payment.helpers.client.mercadopago.schemas.CreatePaymentPayload;
import com.fiap.soat11.payment.helpers.client.mercadopago.schemas.CreatePaymentResponse;
import com.fiap.soat11.payment.helpers.client.mercadopago.schemas.GetPaymentByIdResponse;

public interface MarcadoPagoClient {
    CreatePaymentResponse createPayment(CreatePaymentPayload payload);
    GetPaymentByIdResponse getPaymentById(String paymentId);
}
