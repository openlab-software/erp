package software.openlab.catalog.infra.messaging;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * Thin wrapper around the RabbitMQ Java client. Declares the durable topic
 * exchange "catalog.events" (equivalent to go-common's event.CatalogEvents)
 * at startup and exposes a synchronous publish used by {@link OutboxRelayJob}.
 */
@ApplicationScoped
public class RabbitMqPublisher {

    private static final Logger LOG = Logger.getLogger(RabbitMqPublisher.class);

    @ConfigProperty(name = "catalog.rabbitmq.host", defaultValue = "localhost")
    String host;

    @ConfigProperty(name = "catalog.rabbitmq.port", defaultValue = "5672")
    int port;

    @ConfigProperty(name = "catalog.rabbitmq.username", defaultValue = "root")
    String username;

    @ConfigProperty(name = "catalog.rabbitmq.password", defaultValue = "RabbitMQ123!")
    String password;

    @ConfigProperty(name = "catalog.rabbitmq.exchange", defaultValue = "catalog.events")
    String exchange;

    private Connection connection;
    private Channel channel;

    void onStart(@Observes StartupEvent event) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(host);
            factory.setPort(port);
            factory.setUsername(username);
            factory.setPassword(password);
            factory.setAutomaticRecoveryEnabled(true);

            connection = factory.newConnection("catalog-service");
            channel = connection.createChannel();
            channel.exchangeDeclare(exchange, BuiltinExchangeType.TOPIC, true, false, false, null);

            LOG.infof("[RabbitMQ] connected — exchange=%s host=%s:%d", exchange, host, port);
        } catch (IOException | TimeoutException e) {
            throw new IllegalStateException("failed to connect to RabbitMQ", e);
        }
    }

    void onStop(@Observes ShutdownEvent event) {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
            if (connection != null && connection.isOpen()) {
                connection.close();
            }
        } catch (IOException | TimeoutException e) {
            LOG.warn("[RabbitMQ] error closing connection", e);
        }
    }

    public void publish(String routingKey, String jsonBody) throws IOException {
        channel.basicPublish(
                exchange,
                routingKey,
                MessageProperties.PERSISTENT_TEXT_PLAIN.builder().contentType("application/json").build(),
                jsonBody.getBytes(StandardCharsets.UTF_8)
        );
    }
}
