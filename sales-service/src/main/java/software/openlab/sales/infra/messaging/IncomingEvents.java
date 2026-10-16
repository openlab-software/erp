package software.openlab.sales.infra.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.reactive.messaging.rabbitmq.IncomingRabbitMQMetadata;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.nio.charset.StandardCharsets;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

/**
 * Shared plumbing of the consumers of other modules' events: reads the routing key and the
 * {@code {"event","timestamp","payload"}} envelope, runs the handler on a worker thread (use cases are blocking
 * and transactional), acks on success and nacks (rejected, no requeue) on failure.
 */
public final class IncomingEvents {

    @FunctionalInterface
    public interface Handler {
        void handle(String routingKey, JsonNode payload) throws Exception;
    }

    private IncomingEvents() {
    }

    public static Uni<Void> process(Message<?> message, ObjectMapper mapper, Handler handler, Logger log) {
        String routingKey = message.getMetadata(IncomingRabbitMQMetadata.class)
                .map(IncomingRabbitMQMetadata::getRoutingKey)
                .orElse("");

        return Uni.createFrom().<Void>item(() -> {
                    try {
                        JsonNode envelope = mapper.readTree(toBytes(message.getPayload()));
                        handler.handle(routingKey, envelope.path("payload"));
                    } catch (RuntimeException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                    return null;
                })
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .onItem().transformToUni(ignored -> Uni.createFrom().completionStage(message.ack()))
                .onFailure().recoverWithUni(e -> {
                    log.errorf(e, "failed to process event routing_key=%s; nacking without requeue", routingKey);
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
