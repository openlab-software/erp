package software.openlab.stock.domain.stockcount;

import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import software.openlab.stock.domain.shared.ProductId;
import software.openlab.stock.domain.stock.StockId;

/** Append-only record of a physical inventory count — never updated once created. */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class StockCount {

    private final StockCountId countId;
    private final StockId stockId;
    private final ProductId productId;
    private final int systemValue;
    private final int countedValue;
    private final Instant createdAt;

    public static StockCount create(StockId stockId, ProductId productId, int systemValue, int countedValue) {
        return new StockCount(StockCountId.generate(), stockId, productId, systemValue, countedValue, Instant.now());
    }

    /** Rebuilds a StockCount already persisted — used when loading from storage. */
    public static StockCount reconstruct(StockCountId countId, StockId stockId, ProductId productId,
                                          int systemValue, int countedValue, Instant createdAt) {
        return new StockCount(countId, stockId, productId, systemValue, countedValue, createdAt);
    }
}
