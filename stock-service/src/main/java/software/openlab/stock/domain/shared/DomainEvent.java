package software.openlab.stock.domain.shared;

/**
 * CDI event payload fired via {@code jakarta.enterprise.event.Event<DomainEvent>} by use
 * cases that mutate an aggregate. Observed synchronously by
 * {@code infra.messaging.OutboxEventPublisher}, which runs in the same
 * thread/transaction as the firing use case — required for the outbox insert to commit
 * atomically with the aggregate write (Requirement 14).
 */
public record DomainEvent(String name, Object payload) {
}
