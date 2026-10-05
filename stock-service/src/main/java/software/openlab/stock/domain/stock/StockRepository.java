package software.openlab.stock.domain.stock;

import java.util.Optional;
import software.openlab.stock.domain.shared.PageResult;
import software.openlab.stock.domain.shared.ProductId;

public interface StockRepository {

    PageResult<Stock> findAll(String q, int page, int pageSize);

    Optional<Stock> findById(StockId stockId);

    boolean existsByDescriptionIgnoreCase(String description);

    boolean existsByDescriptionIgnoreCaseExcluding(String description, StockId excludeStockId);

    void insert(Stock stock);

    void update(Stock stock);

    /** Hard-deletes the stock and cascades the delete to its StockItems, in one transaction. */
    void deleteCascade(StockId stockId);

    /** True if any StockItem of this stock currently has {@code current_value > 0}. */
    boolean hasActiveItems(StockId stockId);

    /** True if any StockItem of this stock currently has {@code reserved_value > 0} (Requirement 6.6). */
    boolean hasReservedItems(StockId stockId);

    /** True if this stock is referenced as from_stock_id or to_stock_id in any Reassignment. */
    boolean isReferencedInAnyReassignment(StockId stockId);

    /**
     * Reactive StockItem lifecycle driven by catalog.events (Requirement 12 + legacy
     * product.created behavior). All three are idempotent no-ops when nothing matches,
     * which is how "unknown product" events are silently ignored per Requirement 12.3.
     */
    void createEmptyItemsForProductInAllStocks(ProductId productId);

    void deactivateItemsForProduct(ProductId productId);

    void deleteItemsForProduct(ProductId productId);
}
