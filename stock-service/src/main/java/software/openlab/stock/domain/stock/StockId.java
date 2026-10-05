package software.openlab.stock.domain.stock;

import software.openlab.stock.domain.shared.PrefixedId;

/** Strongly-typed Stock aggregate id, format {@code stock_<ULID>}. */
public record StockId(String value) {

    public static final String PREFIX = "stock";

    public StockId {
        value = PrefixedId.parse(PREFIX, value);
    }

    public static StockId generate() {
        return new StockId(PrefixedId.generate(PREFIX));
    }

    public static StockId of(String raw) {
        return new StockId(raw);
    }

    @Override
    public String toString() {
        return value;
    }
}
