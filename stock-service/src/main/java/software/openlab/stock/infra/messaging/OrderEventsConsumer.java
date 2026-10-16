package software.openlab.stock.infra.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;
import software.openlab.stock.application.usecase.picking.CancelPickingsForOrderUseCase;
import software.openlab.stock.application.usecase.picking.CreatePickingFromOrderUseCase;
import software.openlab.stock.application.usecase.picking.CreatePickingFromOrderUseCase.OrderLine;
import software.openlab.stock.domain.picking.PickingType;

/**
 * Optional integrations of the inventory module: a confirmed sales order creates a delivery and a confirmed
 * purchase order a receipt; cancelling the order cancels the pickings not yet validated. Each consumer declares
 * the producer's exchange itself, so inventory starts the same whether or not those modules are deployed.
 */
@ApplicationScoped
public class OrderEventsConsumer {

    private static final Logger LOG = Logger.getLogger(OrderEventsConsumer.class);

    @Inject
    ObjectMapper mapper;

    @Inject
    CreatePickingFromOrderUseCase createPickingFromOrder;

    @Inject
    CancelPickingsForOrderUseCase cancelPickingsForOrder;

    @Incoming("sales-events")
    public Uni<Void> onSalesEvent(Message<?> message) {
        return IncomingEvents.process(message, mapper, this::handleSales, LOG);
    }

    @Incoming("purchase-events")
    public Uni<Void> onPurchaseEvent(Message<?> message) {
        return IncomingEvents.process(message, mapper, this::handlePurchase, LOG);
    }

    private void handleSales(String routingKey, JsonNode payload) {
        switch (routingKey) {
            case "sales_order.confirmed" -> createPickingFromOrder.execute(PickingType.DELIVERY, "SALES_ORDER",
                    payload.path("id").asText(), payload.path("number").asText(),
                    payload.path("customer_id").asText(null), payload.path("customer_name").asText(null),
                    lines(payload));
            case "sales_order.cancelled" -> cancelPickingsForOrder.execute("SALES_ORDER",
                    payload.path("id").asText());
            default -> LOG.debugf("ignoring sales event %s", routingKey);
        }
    }

    private void handlePurchase(String routingKey, JsonNode payload) {
        switch (routingKey) {
            case "purchase_order.confirmed" -> createPickingFromOrder.execute(PickingType.RECEIPT, "PURCHASE_ORDER",
                    payload.path("id").asText(), payload.path("number").asText(),
                    payload.path("supplier_id").asText(null), payload.path("supplier_name").asText(null),
                    lines(payload));
            case "purchase_order.cancelled" -> cancelPickingsForOrder.execute("PURCHASE_ORDER",
                    payload.path("id").asText());
            default -> LOG.debugf("ignoring purchase event %s", routingKey);
        }
    }

    private static List<OrderLine> lines(JsonNode payload) {
        List<OrderLine> lines = new ArrayList<>();
        for (JsonNode l : payload.path("lines")) {
            lines.add(new OrderLine(l.path("product_id").asText(), l.path("description").asText(null),
                    new BigDecimal(l.path("quantity").asText("0")), l.path("product_type").asText("GOOD")));
        }
        return lines;
    }
}
