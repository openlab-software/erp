package software.openlab.customer.infra.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import software.openlab.customer.domain.shared.DomainEvent;
import software.openlab.customer.infra.persistence.OutboxEntry;

/**
 * Observes {@link DomainEvent}s fired by use cases and writes them to
 * {@code customer.outbox_entries}. The observer method is a plain synchronous
 * CDI observer (no {@code @ObservesAsync}, no {@code TransactionPhase}) so it
 * runs on the same thread and inside the same JTA transaction as the
 * {@code @Transactional} use case that fired the event — the insert here
 * therefore commits atomically with the aggregate write, satisfying
 * Requirement 14. Delivery to RabbitMQ is the separate, asynchronous
 * responsibility of {@link OutboxRelayJob}.
 */
@ApplicationScoped
public class OutboxEventPublisher {

    @Inject
    ObjectMapper objectMapper;

    public void onDomainEvent(@Observes DomainEvent event) {
        try {
            String json = objectMapper.writeValueAsString(EventEnvelope.of(event.name(), event.payload()));

            OutboxEntry entry = new OutboxEntry();
            entry.routingKey = event.name();
            entry.payload = json;
            OutboxEntry.persist(entry);
        } catch (Exception e) {
            throw new IllegalStateException("failed to serialize event " + event.name(), e);
        }
    }
}
