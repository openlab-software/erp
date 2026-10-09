package software.openlab.customer.infra.messaging;

import java.time.Instant;

/**
 * Wire format published to RabbitMQ: {@code {"event": "...", "timestamp": "...", "payload": {...}}}.
 */
public record EventEnvelope(String event, Instant timestamp, Object payload) {

    public static EventEnvelope of(String event, Object payload) {
        return new EventEnvelope(event, Instant.now(), payload);
    }
}
