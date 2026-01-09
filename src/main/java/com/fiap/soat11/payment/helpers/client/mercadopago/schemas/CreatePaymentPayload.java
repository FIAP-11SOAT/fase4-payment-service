package com.fiap.soat11.payment.helpers.client.mercadopago.schemas;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CreatePaymentPayload(
    String title,
    String description,
    @JsonProperty("external_reference") String externalReference,
    @JsonProperty("notification_url") String notificationUrl,
    @JsonProperty("total_amount") Double totalAmount,
    List<Item> items
) {
    
    public record Item(
        String title,
        String category,
        Integer quantity,
        @JsonProperty("unit_measure") String unitMeasure,
        @JsonProperty("unit_price") Double unitPrice,
        @JsonProperty("total_amount") Double totalAmount
    ) {
        public static Item create(String title, String category, Integer quantity, String unitMeasure, Double unitPrice) {
            return new Item(title, category, quantity, unitMeasure, unitPrice, unitPrice * quantity);
        }
    }
    
    public static CreatePaymentPayload create(
        String title,
        String externalReference,
        String notificationUrl,
        Double amount
    ) {
        Item item = new Item(
            title,
            "payment",
            1,
            "unit",
            amount,
            amount
        );
        
        return new CreatePaymentPayload(
            title,
            "Payment for " + title,
            externalReference,
            notificationUrl,
            amount,
            List.of(item)
        );
    }

}
