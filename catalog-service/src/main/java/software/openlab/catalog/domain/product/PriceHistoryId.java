package software.openlab.catalog.domain.product;

import software.openlab.catalog.domain.shared.BadRequestException;
import software.openlab.catalog.domain.shared.Ulid;

/**
 * Strongly-typed public identifier for a {@link PriceHistory} entry, in the
 * form {@code pricehist_<ULID>} (e.g. {@code pricehist_01J6Z3K9QZXJ6R6JYX8G0XJ0KQ}).
 */
public record PriceHistoryId(String value) {

    private static final String PREFIX = "pricehist";
    private static final String PREFIXED = PREFIX + "_";

    public PriceHistoryId {
        if (value == null || !value.startsWith(PREFIXED) || !Ulid.isValid(value.substring(PREFIXED.length()))) {
            throw new BadRequestException("error.id.invalid", PREFIXED + "<ULID>");
        }
    }

    public static PriceHistoryId generate() {
        return new PriceHistoryId(PREFIXED + Ulid.generate());
    }

    public static PriceHistoryId of(String raw) {
        return new PriceHistoryId(raw);
    }

    @Override
    public String toString() {
        return value;
    }
}
