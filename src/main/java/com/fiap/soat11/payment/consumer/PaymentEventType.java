package com.fiap.soat11.payment.consumer;

public enum PaymentEventType {
    ORDER_PAYMENT_REQUESTED("order-payment-requested-event");

    private final String eventName;

    PaymentEventType(String eventName) {
        this.eventName = eventName;
    }

    public String getEventName() {
        return eventName;
    }

    public static PaymentEventType fromEventName(String eventName) {
        for (PaymentEventType type : PaymentEventType.values()) {
            if (type.eventName.equals(eventName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown event name: " + eventName);
    }
}
