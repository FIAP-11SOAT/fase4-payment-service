package com.fiap.soat11.payment.consumer;

import org.springframework.stereotype.Service;

import com.fiap.soat11.payment.dto.ConsumerData;
import com.fiap.soat11.payment.dto.ConsumerMeta;
import com.fiap.soat11.payment.dto.ConsumerPayloadOrder;
import com.fiap.soat11.payment.dto.SendOrderMessageData;
import com.fiap.soat11.payment.dto.SendOrderPayload;
import com.fiap.soat11.payment.dto.SendOrderPayloadPayment;
import com.fiap.soat11.payment.entity.Payment;
import com.fiap.soat11.payment.service.PaymentService;

import io.awspring.cloud.sqs.operations.SqsTemplate;

@Service
public class PaymentConsumerService {

    private final PaymentService paymentService;
    private final SqsTemplate sqsTemplate;

    public PaymentConsumerService(PaymentService paymentService, SqsTemplate sqsTemplate) {
        this.paymentService = paymentService;
        this.sqsTemplate = sqsTemplate;
    }

    public void handler(ConsumerData data) {
        PaymentEventType eventType = PaymentEventType.fromEventName(data.meta().eventName());

        switch (eventType) {
            case ORDER_PAYMENT_REQUESTED:
                this.handleOrderPaymentRequested(data);
                break;
            default:
                System.err.println("Evento desconhecido: " + data.meta().eventName());
        }
    }

    void handleOrderPaymentRequested(ConsumerData data) {
        try {
            ConsumerPayloadOrder order = data.payload().order();

            if (order == null) {
                System.err.println("Order payload is null");
                return;
            }

            Payment payment = paymentService.createPayment(
                    order.id().toString(),
                    order.amount(),
                    order.customerName());

            System.out.println("Pagamento criado com sucesso: " + payment);

            SendOrderMessageData messageData = new SendOrderMessageData(
                    ConsumerMeta.create(
                            this.paymentService.getCurrentTimestamp(),
                            "payment-service",
                            "order-service",
                            "payment-created-event"),
                    new SendOrderPayload(
                            new SendOrderPayloadPayment(
                                    payment.getOrderID(), 
                                    payment.getId()
                                )));
            sqsTemplate.send("fase4-order-service-queue", messageData);

        } catch (Exception e) {
            System.err.println("Erro ao processar pagamento do pedido: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
