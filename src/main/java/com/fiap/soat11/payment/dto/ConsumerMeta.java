package com.fiap.soat11.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ConsumerMeta(
        @JsonProperty("event_id") String eventId,
        @JsonProperty("event_type") String eventDate,
        @JsonProperty("event_source") String eventSource,
        @JsonProperty("event_target") String eventTarget,
        @JsonProperty("event_name") String eventName) {
}
