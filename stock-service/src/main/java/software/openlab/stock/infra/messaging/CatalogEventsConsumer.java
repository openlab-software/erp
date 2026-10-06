package software.openlab.stock.infra.messaging;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.reactive.messaging.rabbitmq.IncomingRabbitMQMetadata;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.nio.charset.StandardCharsets;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

/**
 * Consumes the {@code catalog-events} channel: the durable "stock-subscriber" queue bound to
 * the "catalog.events" topic exchange (see application.properties). Success acks; an
 * unexpected failure nacks, which the connector rejects without requeue
 * ({@code failure-strategy=reject}). Handling runs on a worker thread because the use cases
 * are blocking and transactional.
 */
@ApplicationScoped
public class CatalogEventsConsumer {

    private static final Logger LOG = Logger.getLogger(CatalogEventsConsumer.class);

    @Inject
    CatalogEventHandler catalogEventHandler;

    @Incoming("catalog-events")
    public Uni<Void> consume(Message<?> message) {
        String routingKey = message.getMetadata(IncomingRabbitMQMetadata.class)
                .map(IncomingRabbitMQMetadata::getRoutingKey)
                .orElse("");

        return Uni.createFrom().<Void>item(() -> {
                    try {
                        catalogEventHandler.handle(routingKey, toBytes(message.getPayload()));
                    } catch (java.io.IOException e) {
                        throw new java.io.UncheckedIOException(e);
                    }
                    return null;
                })
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .onItem().transformToUni(ignored -> Uni.createFrom().completionStage(message.ack()))
                .onFailure().recoverWithUni(e -> {
                    LOG.errorf(e, "failed to process catalog event routing_key=%s — nacking without requeue", routingKey);
                    return Uni.createFrom().completionStage(message.nack(e));
                });
    }

    /** The connector may hand JSON bodies over as a Vert.x JsonObject/JsonArray, String or byte[]. */
    private static byte[] toBytes(Object payload) {
        if (payload instanceof byte[] bytes) {
            return bytes;
        }
        if (payload instanceof JsonObject json) {
            return json.encode().getBytes(StandardCharsets.UTF_8);
        }
        if (payload instanceof JsonArray json) {
            return json.encode().getBytes(StandardCharsets.UTF_8);
        }
        return String.valueOf(payload).getBytes(StandardCharsets.UTF_8);
    }
}
