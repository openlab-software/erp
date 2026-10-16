package software.openlab.purchase.infra.messaging;

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
import software.openlab.purchase.application.usecase.order.ApplyReceiptUseCase;
import software.openlab.purchase.application.usecase.order.MarkPurchaseOrderBilledUseCase;
import software.openlab.purchase.domain.order.PurchaseOrderId;

/**
 * Optional integrations. The inventory module publishes {@code picking.done} on {@code stock.events} and the
 * accounting module {@code invoice.posted} on {@code accounting.events}. Without those modules the queues just
 * stay empty, so purchase works the same either way.
 */
@ApplicationScoped
public class IntegrationEventsConsumer {

    private static final Logger LOG = Logger.getLogger(IntegrationEventsConsumer.class);

    private static final String ORIGIN_TYPE = "PURCHASE_ORDER";

    @Inject
    ObjectMapper mapper;

    @Inject
    ApplyReceiptUseCase applyReceipt;

    @Inject
    MarkPurchaseOrderBilledUseCase markBilled;

    @Incoming("stock-events")
    public Uni<Void> onStockEvent(Message<?> message) {
        return IncomingEvents.process(message, mapper, this::handleStock, LOG);
    }

    @Incoming("accounting-events")
    public Uni<Void> onAccountingEvent(Message<?> message) {
        return IncomingEvents.process(message, mapper, this::handleAccounting, LOG);
    }

    private void handleStock(String routingKey, JsonNode payload) {
        if (!"picking.done".equals(routingKey) || !"RECEIPT".equals(payload.path("type").asText())
                || !ORIGIN_TYPE.equals(payload.path("origin_type").asText())) {
            return;
        }
        Map<String, BigDecimal> received = new HashMap<>();
        for (JsonNode line : payload.path("lines")) {
            received.merge(line.path("product_id").asText(), new BigDecimal(line.path("quantity").asText("0")),
                    BigDecimal::add);
        }
        applyReceipt.execute(payload.path("id").asText(), PurchaseOrderId.of(payload.path("origin_id").asText()),
                received);
    }

    private void handleAccounting(String routingKey, JsonNode payload) {
        if (!"invoice.posted".equals(routingKey) || !"VENDOR_BILL".equals(payload.path("kind").asText())
                || !ORIGIN_TYPE.equals(payload.path("origin_type").asText())) {
            return;
        }
        markBilled.execute(payload.path("id").asText(), PurchaseOrderId.of(payload.path("origin_id").asText()));
    }
}
