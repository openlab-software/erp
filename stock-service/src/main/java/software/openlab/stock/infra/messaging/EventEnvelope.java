package software.openlab.stock.infra.messaging;

import java.time.Instant;

/** Wire envelope — mirrors go-common/event.Event: {"event", "timestamp", "payload"}. */
public record EventEnvelope(String event, Instant timestamp, Object payload) {
}
