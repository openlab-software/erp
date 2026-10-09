package software.openlab.customer.infra.messaging;

import io.smallrye.reactive.messaging.MutinyEmitter;
import io.smallrye.reactive.messaging.rabbitmq.OutgoingRabbitMQMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;

/**
 * Publishes to the durable topic exchange "customer.events" through the
 * {@code customer-events} outgoing channel of quarkus-messaging-rabbitmq (exchange
 * declaration and connection settings live in application.properties). The call blocks
 * until the broker has acknowledged the message, so {@link OutboxRelayJob} only marks an
 * entry as published once it was actually sent.
 */
@ApplicationScoped
public class RabbitMqPublisher {

    private static final Duration PUBLISH_TIMEOUT = Duration.ofSeconds(10);

    @Inject
    @Channel("customer-events")
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
