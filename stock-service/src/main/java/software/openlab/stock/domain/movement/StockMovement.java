package software.openlab.stock.domain.movement;

import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import software.openlab.stock.domain.movementreason.MovementReasonId;
import software.openlab.stock.domain.shared.ProductId;
import software.openlab.stock.domain.stock.StockId;

/** Append-only record of a change to a Stock_Item's current_value — never updated once created. */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class StockMovement {

    private final StockMovementId movementId;
    private final StockId stockId;
    private final ProductId productId;
    private final StockMovementType type;
    private final int quantity;
    private final int resultingBalance;
    private final MovementReasonId reasonId;
    private final String reference;
    private final Instant createdAt;

    public static StockMovement create(StockId stockId, ProductId productId, StockMovementType type, int quantity,
                                        int resultingBalance, MovementReasonId reasonId, String reference) {
        return new StockMovement(StockMovementId.generate(), stockId, productId, type, quantity, resultingBalance,
                reasonId, reference, Instant.now());
    }

    /** Rebuilds a StockMovement already persisted — used when loading from storage. */
    public static StockMovement reconstruct(StockMovementId movementId, StockId stockId, ProductId productId,
                                             StockMovementType type, int quantity, int resultingBalance,
                                             MovementReasonId reasonId, String reference, Instant createdAt) {
        return new StockMovement(movementId, stockId, productId, type, quantity, resultingBalance, reasonId,
                reference, createdAt);
    }
}
