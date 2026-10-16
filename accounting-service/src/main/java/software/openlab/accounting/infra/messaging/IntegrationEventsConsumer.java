package software.openlab.accounting.infra.messaging;

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
import software.openlab.accounting.application.usecase.invoice.CreateInvoiceFromOrderUseCase;
import software.openlab.accounting.domain.invoice.InvoiceKind;
import software.openlab.accounting.domain.invoice.InvoiceLine;

/**
 * Optional integrations: a confirmed sales order drafts a customer invoice and a confirmed purchase order a
 * vendor bill. Each consumer declares the producer's exchange itself, so accounting starts the same whether or
 * not the sales / purchase modules are deployed.
 */
@ApplicationScoped
public class IntegrationEventsConsumer {

    private static final Logger LOG = Logger.getLogger(IntegrationEventsConsumer.class);

    @Inject
    ObjectMapper mapper;

    @Inject
    CreateInvoiceFromOrderUseCase createInvoiceFromOrder;

    @Incoming("sales-events")
    public Uni<Void> onSalesEvent(Message<?> message) {
        return IncomingEvents.process(message, mapper, this::handleSales, LOG);
    }

    @Incoming("purchase-events")
    public Uni<Void> onPurchaseEvent(Message<?> message) {
        return IncomingEvents.process(message, mapper, this::handlePurchase, LOG);
    }

    private void handleSales(String routingKey, JsonNode payload) {
        if (!"sales_order.confirmed".equals(routingKey)) {
            return;
        }
        createInvoiceFromOrder.execute(InvoiceKind.CUSTOMER_INVOICE, "SALES_ORDER", payload.path("id").asText(),
                payload.path("number").asText(), payload.path("customer_id").asText(),
                payload.path("customer_name").asText(), payload.path("currency").asText("BRL"), lines(payload));
    }

    private void handlePurchase(String routingKey, JsonNode payload) {
        if (!"purchase_order.confirmed".equals(routingKey)) {
            return;
        }
        createInvoiceFromOrder.execute(InvoiceKind.VENDOR_BILL, "PURCHASE_ORDER", payload.path("id").asText(),
                payload.path("number").asText(), payload.path("supplier_id").asText(),
                payload.path("supplier_name").asText(), payload.path("currency").asText("BRL"), lines(payload));
    }

    private static List<InvoiceLine> lines(JsonNode payload) {
        List<InvoiceLine> lines = new ArrayList<>();
        for (JsonNode l : payload.path("lines")) {
            lines.add(new InvoiceLine(
                    l.path("product_id").asText(null),
                    l.path("description").asText(),
                    decimal(l, "quantity"),
                    decimal(l, "unit_price"),
                    decimal(l, "discount_percent"),
                    decimal(l, "tax_percent")));
        }
        return lines;
    }

    private static BigDecimal decimal(JsonNode node, String field) {
        return node.hasNonNull(field) ? new BigDecimal(node.get(field).asText()) : BigDecimal.ZERO;
    }
}
