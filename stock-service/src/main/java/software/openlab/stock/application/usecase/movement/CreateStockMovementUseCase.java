package software.openlab.stock.application.usecase.movement;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.EnumSet;
import java.util.Set;
import software.openlab.stock.application.usecase.stockitem.GetStockItemUseCase;
import software.openlab.stock.domain.movement.StockMovement;
import software.openlab.stock.domain.movement.StockMovementType;
import software.openlab.stock.domain.movementreason.MovementReasonId;
import software.openlab.stock.domain.movementreason.MovementReasonRepository;
import software.openlab.stock.domain.shared.BadRequestException;
import software.openlab.stock.domain.shared.ProductId;
import software.openlab.stock.domain.stock.StockId;
import software.openlab.stock.domain.stockitem.StockItem;

/** POST /v1/stocks/{id}/movements — Requirement 3 of stock-master-data. */
@ApplicationScoped
public class CreateStockMovementUseCase {

    private static final Set<StockMovementType> ALLOWED_TYPES =
            EnumSet.of(StockMovementType.ENTRY, StockMovementType.EXIT, StockMovementType.ADJUSTMENT);

    @Inject
    GetStockItemUseCase getStockItem;

    @Inject
    MovementReasonRepository movementReasonRepository;

    @Inject
    ApplyStockMovementUseCase applyStockMovement;

    @Transactional
    public StockMovement execute(StockId stockId, ProductId productId, String rawType, int quantity,
                                  String rawReasonId, String reference) {
        StockMovementType type = parseType(rawType);

        // Throws 404 if the stock or the item for this product in it does not exist.
        StockItem item = getStockItem.execute(stockId, productId);

        MovementReasonId reasonId = null;
        int delta;
        switch (type) {
            case ENTRY -> {
                if (quantity <= 0) {
                    throw new BadRequestException("error.movement.quantityPositive", "ENTRY");
                }
                delta = quantity;
            }
            case EXIT -> {
                if (quantity <= 0) {
                    throw new BadRequestException("error.movement.quantityPositive", "EXIT");
                }
                delta = -quantity;
            }
            case ADJUSTMENT -> {
                if (quantity == 0) {
                    throw new BadRequestException("error.movement.quantityZero");
                }
                if (rawReasonId == null || rawReasonId.isBlank()) {
                    throw new BadRequestException("error.movement.reasonRequired");
                }
                reasonId = MovementReasonId.of(rawReasonId);
                if (movementReasonRepository.findById(reasonId).isEmpty()) {
                    throw new BadRequestException("error.movementReason.refInvalid");
                }
                delta = quantity;
            }
            default -> throw new BadRequestException("error.movement.typeInvalid");
        }

        return applyStockMovement.apply(stockId, productId, item.currentValue(), type, delta, quantity, reasonId,
                reference);
    }

    private StockMovementType parseType(String rawType) {
        StockMovementType type;
        try {
            type = StockMovementType.valueOf(rawType);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BadRequestException("error.movement.typeInvalid");
        }
        if (!ALLOWED_TYPES.contains(type)) {
            throw new BadRequestException("error.movement.typeInvalid");
        }
        return type;
    }
}
