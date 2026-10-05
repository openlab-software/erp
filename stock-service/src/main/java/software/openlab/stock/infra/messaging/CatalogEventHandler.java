package software.openlab.stock.infra.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import org.jboss.logging.Logger;
import software.openlab.stock.application.usecase.stock.HandleProductCreatedUseCase;
import software.openlab.stock.application.usecase.stock.HandleProductDeletedUseCase;
import software.openlab.stock.application.usecase.stock.HandleProductInactivatedUseCase;
import software.openlab.stock.domain.shared.ProductId;

/**
 * Handles catalog.events deliveries (product.created / product.updated / product.deleted),
 * per Requirement 12.
 *
 * <p>The exact JSON key carrying the product's public id is not fixed by the spec text
 * consistently ("id" in the Requirement 5/7 prose vs. "product_id" already used by the
 * existing product.created event) and catalog-service is being reimplemented independently
 * in parallel, so both keys are accepted defensively.
 */
@ApplicationScoped
public class CatalogEventHandler {

    private static final Logger LOG = Logger.getLogger(CatalogEventHandler.class);

    static final String PRODUCT_CREATED = "product.created";
    static final String PRODUCT_UPDATED = "product.updated";
    static final String PRODUCT_DELETED = "product.deleted";
    static final String PRODUCT_TYPE_SERVICE = "SERVICE";

    @Inject
    HandleProductCreatedUseCase handleProductCreated;

    @Inject
    HandleProductInactivatedUseCase handleProductInactivated;

    @Inject
    HandleProductDeletedUseCase handleProductDeleted;

    @Inject
    ObjectMapper objectMapper;

    public void handle(String routingKey, byte[] body) throws IOException {
        JsonNode envelope = objectMapper.readTree(body);
        JsonNode payload = envelope.path("payload");
        ProductId productId = ProductId.of(extractProductPublicId(payload));

        switch (routingKey) {
            case PRODUCT_CREATED -> handleProductCreated(payload, productId);
            case PRODUCT_UPDATED -> handleProductUpdated(payload, productId);
            case PRODUCT_DELETED -> handleProductDeleted.execute(productId);
            default -> LOG.warnf("ignoring catalog event with unbound routing_key=%s", routingKey);
        }
    }

    private void handleProductCreated(JsonNode payload, ProductId productId) {
        // Per Requirement 7 of catalog-master-data: a missing "type" is a legacy message
        // (pre-dating the field on catalog-service) and is treated as GOOD for backward
        // compatibility.
        String type = payload.path("type").asText(null);
        if (PRODUCT_TYPE_SERVICE.equals(type)) {
            LOG.debugf("skipping StockItem creation for SERVICE product_id=%s", productId);
            return;
        }
        handleProductCreated.execute(productId);
    }

    private void handleProductUpdated(JsonNode payload, ProductId productId) {
        String status = payload.path("status").asText(null);
        if ("INACTIVE".equals(status)) {
            handleProductInactivated.execute(productId);
        }
        // Other status transitions (DRAFT/PUBLISHED/ACTIVE) require no stock-side reaction.
    }

    private String extractProductPublicId(JsonNode payload) {
        if (payload.hasNonNull("id")) {
            return payload.get("id").asText();
        }
        if (payload.hasNonNull("product_id")) {
            return payload.get("product_id").asText();
        }
        throw new IllegalArgumentException("catalog event payload is missing the product id field");
    }
}
