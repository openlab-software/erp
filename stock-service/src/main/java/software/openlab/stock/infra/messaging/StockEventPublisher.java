package software.openlab.stock.infra.messaging;

import io.smallrye.reactive.messaging.MutinyEmitter;
import io.smallrye.reactive.messaging.rabbitmq.OutgoingRabbitMQMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;

/**
 * Publishes to the durable topic exchange "stock.events" through the {@code stock-events}
 * outgoing channel of quarkus-messaging-rabbitmq. Blocks until the broker acknowledges the
 * message so {@link OutboxRelay} only marks an entry as published once it was sent.
 */
@ApplicationScoped
public class StockEventPublisher {

    private static final Duration PUBLISH_TIMEOUT = Duration.ofSeconds(10);

    @Inject
    @Channel("stock-events")
    MutinyEmitter<String> emitter;

    public void publish(String routingKey, String jsonBody) {
        OutgoingRabbitMQMetadata metadata = new OutgoingRabbitMQMetadata.Builder()
                .withRoutingKey(routingKey)
                .withContentType("application/json")
                .withDeliveryMode(2) // persistent
                .build();
        emitter.sendMessage(Message.of(jsonBody, Metadata.of(metadata)))
                .await().atMost(PUBLISH_TIMEOUT);
    }
}
