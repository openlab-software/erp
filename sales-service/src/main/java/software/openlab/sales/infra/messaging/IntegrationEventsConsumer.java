package software.openlab.sales.infra.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;
import software.openlab.sales.application.usecase.order.ApplyDeliveryUseCase;
import software.openlab.sales.application.usecase.order.MarkSalesOrderInvoicedUseCase;
import software.openlab.sales.domain.order.SalesOrderId;

/**
 * Optional integrations. The inventory module publishes {@code picking.done} on {@code stock.events} and the
 * accounting module {@code invoice.posted} on {@code accounting.events}. Without those modules the queues just
 * stay empty, so sales works the same either way.
 */
@ApplicationScoped
public class IntegrationEventsConsumer {

    private static final Logger LOG = Logger.getLogger(IntegrationEventsConsumer.class);

    private static final String ORIGIN_TYPE = "SALES_ORDER";

    @Inject
    ObjectMapper mapper;

    @Inject
    ApplyDeliveryUseCase applyDelivery;

    @Inject
    MarkSalesOrderInvoicedUseCase markInvoiced;

    @Incoming("stock-events")
    public Uni<Void> onStockEvent(Message<?> message) {
        return IncomingEvents.process(message, mapper, this::handleStock, LOG);
    }

    @Incoming("accounting-events")
    public Uni<Void> onAccountingEvent(Message<?> message) {
        return IncomingEvents.process(message, mapper, this::handleAccounting, LOG);
    }

    private void handleStock(String routingKey, JsonNode payload) {
        if (!"picking.done".equals(routingKey) || !"DELIVERY".equals(payload.path("type").asText())
                || !ORIGIN_TYPE.equals(payload.path("origin_type").asText())) {
            return;
        }
        Map<String, BigDecimal> delivered = new HashMap<>();
        for (JsonNode line : payload.path("lines")) {
            delivered.merge(line.path("product_id").asText(), new BigDecimal(line.path("quantity").asText("0")),
                    BigDecimal::add);
        }
        applyDelivery.execute(payload.path("id").asText(), SalesOrderId.of(payload.path("origin_id").asText()),
                delivered);
    }

    private void handleAccounting(String routingKey, JsonNode payload) {
        if (!"invoice.posted".equals(routingKey) || !"CUSTOMER_INVOICE".equals(payload.path("kind").asText())
                || !ORIGIN_TYPE.equals(payload.path("origin_type").asText())) {
            return;
        }
        markInvoiced.execute(payload.path("id").asText(), SalesOrderId.of(payload.path("origin_id").asText()));
    }
}
