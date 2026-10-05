package software.openlab.stock.domain.stockitem;

import java.util.List;
import java.util.Optional;
import software.openlab.stock.domain.shared.PageResult;
import software.openlab.stock.domain.shared.ProductId;
import software.openlab.stock.domain.stock.StockId;

public interface StockItemRepository {

    /**
     * @param productId optional filter, only the item of this product when present
     * @param belowMin  when {@code true}, only items with {@code minValue != null and currentValue < minValue}
     * @param aboveMax  when {@code true}, only items with {@code maxValue != null and currentValue > maxValue}
     */
    PageResult<StockItem> findByStock(StockId stockId, ProductId productId, Boolean belowMin, Boolean aboveMax,
                                       int page, int pageSize);

    Optional<StockItem> findByStockAndProduct(StockId stockId, ProductId productId);

    /**
     * Persists a new {@code current_value} for the item, applied by
     * {@code CreateStockMovementUseCase} within the same transaction as the Stock_Movement
     * insert. Callers are expected to have already checked existence (e.g. via
     * {@code GetStockItemUseCase}).
     */
    void updateCurrentValue(StockId stockId, ProductId productId, int newCurrentValue);

    /**
     * Persists a new {@code reserved_value} for the item (Requirement 6). Does not touch
     * {@code current_value} and never generates a Stock_Movement. Callers are expected to have
     * already checked existence (e.g. via {@code GetStockItemUseCase}).
     */
    void updateReservedValue(StockId stockId, ProductId productId, int newReservedValue);

    /**
     * Ensures a Stock_Item exists for this product in this stock, creating one with
     * {@code current_value=0}, {@code min_value}/{@code max_value=null}, {@code active=true} if
     * absent. Used by Reassignment (Requirement 5.3) when crediting a destination stock that has
     * never held this product before.
     */
    void ensureItemExists(StockId stockId, ProductId productId);

    /**
     * All {@code active=true} Stock_Item rows for this product across every stock (Requirement 9
     * — consolidated balance). Returns an empty list if none exist; never throws
     * NotFoundException — a product with no tracked balance anywhere is a valid, non-error state.
     */
    List<StockItem> findAllByProduct(ProductId productId);
}
