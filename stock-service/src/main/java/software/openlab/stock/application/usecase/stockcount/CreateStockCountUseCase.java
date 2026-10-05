package software.openlab.stock.application.usecase.stockcount;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.stock.application.usecase.movement.ApplyStockMovementUseCase;
import software.openlab.stock.application.usecase.stockitem.GetStockItemUseCase;
import software.openlab.stock.domain.movement.StockMovementType;
import software.openlab.stock.domain.shared.BadRequestException;
import software.openlab.stock.domain.shared.ProductId;
import software.openlab.stock.domain.stock.StockId;
import software.openlab.stock.domain.stockcount.StockCount;
import software.openlab.stock.domain.stockcount.StockCountRepository;
import software.openlab.stock.domain.stockitem.StockItem;

/**
 * POST /v1/stocks/{id}/counts — Requirement 7. Reuses {@link ApplyStockMovementUseCase} for the
 * auto-generated ADJUSTMENT when {@code counted_value != system_value}, per Requirement 7.2 —
 * unlike a manually-triggered ADJUSTMENT (Requirement 3.3), no reason_id is required here since
 * the physical count itself is the reason.
 */
@ApplicationScoped
public class CreateStockCountUseCase {

    @Inject
    GetStockItemUseCase getStockItem;

    @Inject
    StockCountRepository stockCountRepository;

    @Inject
    ApplyStockMovementUseCase applyStockMovement;

    @Transactional
    public StockCount execute(StockId stockId, ProductId productId, int countedValue) {
        if (countedValue < 0) {
            throw new BadRequestException("error.stockCount.valueInvalid");
        }

        // Throws 404 if the stock or the item for this product in it does not exist.
        StockItem item = getStockItem.execute(stockId, productId);
        int systemValue = item.currentValue();

        StockCount count = StockCount.create(stockId, productId, systemValue, countedValue);
        stockCountRepository.insert(count);

        if (countedValue != systemValue) {
            int delta = countedValue - systemValue;
            applyStockMovement.apply(stockId, productId, systemValue, StockMovementType.ADJUSTMENT, delta, delta,
                    null, count.getCountId().toString());
        }

        return count;
    }
}
