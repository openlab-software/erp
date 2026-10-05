package software.openlab.stock.infra.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import java.time.Instant;
import software.openlab.stock.domain.shared.DomainEvent;
import software.openlab.stock.infra.persistence.OutboxEntryEntity;

/**
 * Equivalent of go-common/outbox.OutboxPublisher: persists the event to
 * stock.outbox_entries. Observes {@link DomainEvent} synchronously (plain {@code @Observes},
 * not {@code @ObservesAsync} nor {@code TransactionPhase.AFTER_SUCCESS}) so it runs on the
 * same thread and inside the same JTA transaction as the use case that fired the event —
 * the aggregate write and the outbox insert commit or roll back together, per Requirement 14 —
 * no explicit transaction-manager plumbing needed, unlike the Go implementation's
 * context-propagated *gorm.DB transaction.
 */
@ApplicationScoped
public class OutboxEventPublisher {

    @Inject
    ObjectMapper objectMapper;

    public void onDomainEvent(@Observes DomainEvent event) {
        String json;
        try {
            json = objectMapper.writeValueAsString(new EventEnvelope(event.name(), Instant.now(), event.payload()));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("failed to serialize domain event payload for " + event.name(), e);
        }

        OutboxEntryEntity entry = new OutboxEntryEntity();
        entry.routingKey = event.name();
        entry.payload = json;
        entry.status = OutboxEntryEntity.PENDING;
        entry.createdAt = Instant.now();
        entry.persist();
    }
}
