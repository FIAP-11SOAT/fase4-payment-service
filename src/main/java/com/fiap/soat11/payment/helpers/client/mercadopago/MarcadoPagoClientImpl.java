package com.fiap.soat11.payment.helpers.client.mercadopago;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fiap.soat11.payment.helpers.client.mercadopago.schemas.CreatePaymentPayload;
import com.fiap.soat11.payment.helpers.client.mercadopago.schemas.CreatePaymentResponse;
import com.fiap.soat11.payment.helpers.client.mercadopago.schemas.GetPaymentByIdResponse;

@Component
public class MarcadoPagoClientImpl implements MarcadoPagoClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String accessToken;
    private final String userID;
    private final String externalPosID;

    public MarcadoPagoClientImpl(
            RestTemplate restTemplate,
            @Value("${fase4.payment.service.marcadopago.accessToken}") String accessToken,
            @Value("${fase4.payment.service.marcadopago.userID}") String userID,
            @Value("${fase4.payment.service.marcadopago.externalPosID}") String externalPosID) {
        this.restTemplate = restTemplate;
        this.baseUrl = "https://api.mercadopago.com";
        this.accessToken = accessToken;
        this.userID = userID;
        this.externalPosID = externalPosID;
    }

    @Override
    public CreatePaymentResponse createPayment(CreatePaymentPayload payload) {
        String url = baseUrl + "/instore/orders/qr/seller/collectors/" + this.userID + "/pos/" + this.externalPosID
                + "/qrs";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(this.accessToken);

        HttpEntity<CreatePaymentPayload> request = new HttpEntity<CreatePaymentPayload>(payload, headers);

        ResponseEntity<CreatePaymentResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                CreatePaymentResponse.class);

        return response.getBody();
    }

    @Override
    public GetPaymentByIdResponse getPaymentById(String paymentId) {
        String url = baseUrl + "/v1/payments/" + paymentId;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(this.accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<GetPaymentByIdResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                GetPaymentByIdResponse.class);

        return response.getBody();
    }

}
