package com.fiap.soat11.payment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;


@DynamoDbBean
@Getter
@Setter
public class Payment {
    private String id;

    @JsonProperty("order_id")
    private String orderID;

    private PaymentStatusEnum status = PaymentStatusEnum.PENDING;

    private Double amount;

    @JsonProperty("integration_id")
    private String integrationID;

    @JsonProperty("integration_qr_code")
    private String integrationQrCode;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }

}
