package com.fiap.soat11.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public record ConsumerMeta(
                @JsonProperty("event_id") String eventId,
                @JsonProperty("event_date") String eventDate,
                @JsonProperty("event_source") String eventSource,
                @JsonProperty("event_target") String eventTarget,
                @JsonProperty("event_name") String eventName) {

        public static ConsumerMeta create(String eventIsoDate, String eventSource, String eventTarget, String eventName) {
                return new ConsumerMeta(
                                UUID.randomUUID().toString(),
                                eventIsoDate,
                                eventSource,
                                eventTarget,
                                eventName);
        }

        public static ConsumerMeta createWithId(String eventId, String eventType, String eventSource,
                        String eventTarget, String eventName) {
                return new ConsumerMeta(eventId, eventType, eventSource, eventTarget, eventName);
        }
}
