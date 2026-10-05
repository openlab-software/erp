package software.openlab.stock.domain.stockcount;

import software.openlab.stock.domain.shared.PageResult;
import software.openlab.stock.domain.shared.ProductId;
import software.openlab.stock.domain.stock.StockId;

public interface StockCountRepository {

    void insert(StockCount count);

    /** @param productId optional filter, only counts of this product when present */
    PageResult<StockCount> findByStock(StockId stockId, ProductId productId, int page, int pageSize);
}
