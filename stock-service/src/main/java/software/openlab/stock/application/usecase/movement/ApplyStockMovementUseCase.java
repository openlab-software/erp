package software.openlab.stock.application.usecase.movement;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.stock.domain.movement.StockMovement;
import software.openlab.stock.domain.movement.StockMovementCreatedPayload;
import software.openlab.stock.domain.movement.StockMovementEvents;
import software.openlab.stock.domain.movement.StockMovementRepository;
import software.openlab.stock.domain.movement.StockMovementType;
import software.openlab.stock.domain.movementreason.MovementReasonId;
import software.openlab.stock.domain.shared.ConflictException;
import software.openlab.stock.domain.shared.DomainEvent;
import software.openlab.stock.domain.shared.ProductId;
import software.openlab.stock.domain.stock.StockId;
import software.openlab.stock.domain.stockitem.StockItemRepository;

/**
 * Core mutation shared by manual movements (Requirement 3), Reassignment transfers
 * (Requirement 5) and Stock_Count adjustments (Requirement 7): computes the new
 * {@code current_value} from {@code delta}, persists it, writes the append-only Stock_Movement
 * row with the post-application {@code resulting_balance}, and fires {@code stock_movement.created}
 * — all expected to run inside the caller's own {@code @Transactional} boundary (type/quantity
 * validation and existence checks are each caller's responsibility, since they differ per
 * caller — e.g. ADJUSTMENT requires a reason_id only when triggered manually, not from a
 * Stock_Count).
 */
@ApplicationScoped
public class ApplyStockMovementUseCase {

    @Inject
    StockItemRepository stockItemRepository;

    @Inject
    StockMovementRepository stockMovementRepository;

    @Inject
    Event<DomainEvent> events;

    /**
     * @param currentValue    the item's current_value BEFORE this movement (caller already holds it)
     * @param delta           signed amount to apply to current_value (negative for outbound movements)
     * @param quantityToRecord the value stored on Stock_Movement.quantity — the positive magnitude
     *                          for ENTRY/EXIT/TRANSFER_IN/TRANSFER_OUT, or the signed delta itself for ADJUSTMENT
     * @throws ConflictException if applying {@code delta} would make current_value negative
     */
    @Transactional
    public StockMovement apply(StockId stockId, ProductId productId, int currentValue, StockMovementType type,
                                int delta, int quantityToRecord, MovementReasonId reasonId, String reference) {
        int newBalance = currentValue + delta;
        if (newBalance < 0) {
            throw new ConflictException("error.movement.negativeBalance");
        }

        stockItemRepository.updateCurrentValue(stockId, productId, newBalance);

        StockMovement movement = StockMovement.create(stockId, productId, type, quantityToRecord, newBalance,
                reasonId, reference);
        stockMovementRepository.insert(movement);

        events.fire(new DomainEvent(StockMovementEvents.CREATED,
                new StockMovementCreatedPayload(movement.getMovementId().toString(), stockId.toString(),
                        productId.toString(), type.name(), quantityToRecord, newBalance)));

        return movement;
    }
}
