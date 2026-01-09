package com.fiap.soat11.payment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import com.fiap.soat11.payment.helpers.client.mercadopago.MarcadoPagoClient;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

@SpringBootTest
@TestPropertySource(properties = {
	"spring.config.import=optional:aws-secretsmanager:fase4-payment-service-secrets"
})
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
