package software.openlab.stock.domain.movement;

/** Payload of {@code stock_movement.created} per Requirement 3.5: no reason_id/reference. */
public record StockMovementCreatedPayload(String id, String stockId, String productId, String type, int quantity,
                                           int resultingBalance) {
}
