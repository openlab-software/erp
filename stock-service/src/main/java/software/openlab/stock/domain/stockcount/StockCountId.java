package software.openlab.stock.domain.stockcount;

import software.openlab.stock.domain.shared.PrefixedId;

/** Strongly-typed StockCount aggregate id, format {@code count_<ULID>}. */
public record StockCountId(String value) {

    public static final String PREFIX = "count";

    public StockCountId {
        value = PrefixedId.parse(PREFIX, value);
    }

    public static StockCountId generate() {
        return new StockCountId(PrefixedId.generate(PREFIX));
    }

    public static StockCountId of(String raw) {
        return new StockCountId(raw);
    }

    @Override
    public String toString() {
        return value;
    }
}
