package software.openlab.stock.application.usecase.stockitem;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.stock.domain.shared.BadRequestException;
import software.openlab.stock.domain.shared.ConflictException;
import software.openlab.stock.domain.shared.ProductId;
import software.openlab.stock.domain.stock.StockId;
import software.openlab.stock.domain.stockitem.StockItem;
import software.openlab.stock.domain.stockitem.StockItemRepository;

/**
 * POST /v1/stocks/{id}/items/{productId}/release — Requirement 6.4/6.5. Never touches
 * current_value and never generates a Stock_Movement.
 */
@ApplicationScoped
public class ReleaseStockUseCase {

    @Inject
    StockItemRepository stockItemRepository;

    @Inject
    GetStockItemUseCase getStockItem;

    @Transactional
    public StockItem execute(StockId stockId, ProductId productId, int quantity) {
        if (quantity <= 0) {
            throw new BadRequestException("error.quantity.positive");
        }

        StockItem item = getStockItem.execute(stockId, productId);
        if (quantity > item.reservedValue()) {
            throw new ConflictException("error.stockItem.releaseExceeds");
        }

        int newReservedValue = item.reservedValue() - quantity;
        stockItemRepository.updateReservedValue(stockId, productId, newReservedValue);

        return new StockItem(item.stockId(), item.productId(), item.minValue(), item.currentValue(),
                item.maxValue(), newReservedValue, item.active());
    }
}
