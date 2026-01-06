package com.fiap.soat11.payment.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.fiap.soat11.payment.entity.Payment;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
public class PaymentRepository {

    private final DynamoDbTable<Payment> paymentTable;

    public PaymentRepository(DynamoDbEnhancedClient client) {
        this.paymentTable = client.table(
            "fase4-payment-service-payments",
            TableSchema.fromBean(Payment.class)
        );
    }

    public Payment save(Payment payment) {
        paymentTable.putItem(payment);
        return payment;
    }

    public Optional<Payment> findById(String id) {
        Key key = Key.builder()
            .partitionValue(id)
            .build();
        
        Payment payment = paymentTable.getItem(key);
        return Optional.ofNullable(payment);
    }

    public Payment update(Payment payment) {
        return paymentTable.updateItem(payment);
    }
}
