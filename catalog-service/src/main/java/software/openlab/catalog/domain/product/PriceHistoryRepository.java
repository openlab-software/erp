package software.openlab.catalog.domain.product;

import java.util.List;

public interface PriceHistoryRepository {

    void insert(PriceHistory priceHistory);

    /** Ordered by {@code changedAt} descending (most recent first). */
    List<PriceHistory> findByProductId(ProductId productId);
}
