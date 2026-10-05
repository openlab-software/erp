package software.openlab.stock.infra.rest.dto;

import java.time.Instant;
import software.openlab.stock.domain.movement.StockMovement;

public record StockMovementResponse(String movementId, String stockId, String productId, String type, int quantity,
                                     int resultingBalance, String reasonId, String reference, Instant createdAt) {

    public static StockMovementResponse from(StockMovement movement) {
        return new StockMovementResponse(
                movement.getMovementId().toString(),
                movement.getStockId().toString(),
                movement.getProductId().toString(),
                movement.getType().name(),
                movement.getQuantity(),
                movement.getResultingBalance(),
                movement.getReasonId() == null ? null : movement.getReasonId().toString(),
                movement.getReference(),
                movement.getCreatedAt());
    }
}
