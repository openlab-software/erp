package software.openlab.catalog.domain.shared;

/**
 * The CDI event payload fired by use cases after a domain mutation. Observed
 * synchronously by {@code infra.messaging.OutboxEventPublisher} — see that
 * class for why the observer must stay synchronous (Requirement 14 atomicity).
 */
public record DomainEvent(String name, Object payload) {
}
