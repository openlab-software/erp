package software.openlab.stock.domain.stock;

import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Stock {

    private final StockId stockId;
    private String description;
    private final Instant createdAt;
    private Instant modifiedAt;

    public static Stock create(String description) {
        return new Stock(StockId.generate(), description, Instant.now(), null);
    }

    /** Rebuilds a Stock already persisted — used when loading from storage. */
    public static Stock reconstruct(StockId stockId, String description, Instant createdAt, Instant modifiedAt) {
        return new Stock(stockId, description, createdAt, modifiedAt);
    }

    public void rename(String newDescription) {
        this.description = newDescription;
        this.modifiedAt = Instant.now();
    }
}
