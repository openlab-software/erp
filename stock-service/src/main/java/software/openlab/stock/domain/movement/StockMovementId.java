package software.openlab.stock.domain.movement;

import software.openlab.stock.domain.shared.PrefixedId;

/** Strongly-typed StockMovement aggregate id, format {@code mov_<ULID>}. */
public record StockMovementId(String value) {

    public static final String PREFIX = "mov";

    public StockMovementId {
        value = PrefixedId.parse(PREFIX, value);
    }

    public static StockMovementId generate() {
        return new StockMovementId(PrefixedId.generate(PREFIX));
    }

    public static StockMovementId of(String raw) {
        return new StockMovementId(raw);
    }

    @Override
    public String toString() {
        return value;
    }
}
