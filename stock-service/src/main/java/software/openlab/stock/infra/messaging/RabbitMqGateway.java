package software.openlab.stock.infra.messaging;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * Hand-rolled RabbitMQ wiring mirroring libs/go-common/rabbitmq, kept local to this
 * service per the "no shared Java lib" decision: declares the "stock.events" topic
 * exchange for publishing, and consumes "catalog.events" on the durable "stock-subscriber"
 * queue with manual ack — success acks, an unexpected failure nacks without requeue
 * (matching the Go subscriber's default), and a handled "unknown product" case (Req 12.3)
 * is treated as a successful no-op and acked normally by {@link CatalogEventHandler}'s
 * idempotent repository operations.
 */
@ApplicationScoped
public class RabbitMqGateway {

    private static final Logger LOG = Logger.getLogger(RabbitMqGateway.class);

    static final String STOCK_EXCHANGE = "stock.events";
    static final String CATALOG_EXCHANGE = "catalog.events";
    static final String SUBSCRIBER_QUEUE = "stock-subscriber";

    @ConfigProperty(name = "rabbitmq.host")
    String host;

    @ConfigProperty(name = "rabbitmq.port")
    int port;

    @ConfigProperty(name = "rabbitmq.username")
    String username;

    @ConfigProperty(name = "rabbitmq.password")
    String password;

    @Inject
    CatalogEventHandler catalogEventHandler;

    private Connection connection;
    private Channel publisherChannel;
    private Channel consumerChannel;

    void onStart(@Observes StartupEvent ev) {
        try {
            connect();
        } catch (IOException | TimeoutException e) {
            throw new IllegalStateException("failed to initialize RabbitMQ connection", e);
        }
    }

    void onStop(@Observes ShutdownEvent ev) {
        closeQuietly();
    }

    private void connect() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setAutomaticRecoveryEnabled(true);
        factory.setNetworkRecoveryInterval(5000);

        connection = factory.newConnection("stock-service");

        publisherChannel = connection.createChannel();
        publisherChannel.exchangeDeclare(STOCK_EXCHANGE, BuiltinExchangeType.TOPIC, true);

        consumerChannel = connection.createChannel();
        consumerChannel.exchangeDeclare(CATALOG_EXCHANGE, BuiltinExchangeType.TOPIC, true);
        consumerChannel.queueDeclare(SUBSCRIBER_QUEUE, true, false, false, null);
        for (String routingKey : List.of("product.created", "product.updated", "product.deleted")) {
            consumerChannel.queueBind(SUBSCRIBER_QUEUE, CATALOG_EXCHANGE, routingKey);
            LOG.infof("bound queue '%s' to routing key '%s'", SUBSCRIBER_QUEUE, routingKey);
        }
        consumerChannel.basicQos(10);

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            long deliveryTag = delivery.getEnvelope().getDeliveryTag();
            String routingKey = delivery.getEnvelope().getRoutingKey();
            try {
                catalogEventHandler.handle(routingKey, delivery.getBody());
                consumerChannel.basicAck(deliveryTag, false);
            } catch (Exception e) {
                LOG.errorf(e, "failed to process catalog event routing_key=%s — nacking without requeue", routingKey);
                consumerChannel.basicNack(deliveryTag, false, false);
            }
        };

        consumerChannel.basicConsume(SUBSCRIBER_QUEUE, false, deliverCallback, consumerTag -> {
        });

        LOG.infof("RabbitMQ gateway ready — publishing to '%s', subscribing on '%s'", STOCK_EXCHANGE, SUBSCRIBER_QUEUE);
    }

    public synchronized void publish(String routingKey, byte[] body) throws IOException {
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                .contentType("application/json")
                .build();
        publisherChannel.basicPublish(STOCK_EXCHANGE, routingKey, props, body);
    }

    private void closeQuietly() {
        try {
            if (consumerChannel != null && consumerChannel.isOpen()) {
                consumerChannel.close();
            }
        } catch (Exception ignored) {
        }
        try {
            if (publisherChannel != null && publisherChannel.isOpen()) {
                publisherChannel.close();
            }
        } catch (Exception ignored) {
        }
        try {
            if (connection != null && connection.isOpen()) {
                connection.close();
            }
        } catch (Exception ignored) {
        }
    }
}
