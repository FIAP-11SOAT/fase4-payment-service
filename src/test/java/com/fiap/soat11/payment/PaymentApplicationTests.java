package com.fiap.soat11.payment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.fiap.soat11.payment.helpers.client.mercadopago.MarcadoPagoClient;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

@SpringBootTest
class PaymentApplicationTests {

	@MockBean
	private DynamoDbEnhancedClient dynamoDbEnhancedClient;

	@MockBean
	private SqsTemplate sqsTemplate;

	@MockBean
	private MarcadoPagoClient marcadoPagoClient;

	@Test
	void contextLoads() {
	}

}
