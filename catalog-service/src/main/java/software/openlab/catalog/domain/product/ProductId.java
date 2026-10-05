package software.openlab.catalog.domain.product;

import software.openlab.catalog.domain.shared.BadRequestException;
import software.openlab.catalog.domain.shared.Ulid;

/**
 * Strongly-typed public identifier for a {@link Product}, in the form
 * {@code prod_<ULID>} (e.g. {@code prod_01J6Z3K9QZXJ6R6JYX8G0XJ0KQ}).
 */
public record ProductId(String value) {

    private static final String PREFIX = "prod";
    private static final String PREFIXED = PREFIX + "_";

    public ProductId {
        if (value == null || !value.startsWith(PREFIXED) || !Ulid.isValid(value.substring(PREFIXED.length()))) {
            throw new BadRequestException("error.id.invalid", PREFIXED + "<ULID>");
        }
    }

    public static ProductId generate() {
        return new ProductId(PREFIXED + Ulid.generate());
    }

    public static ProductId of(String raw) {
        return new ProductId(raw);
    }

    @Override
    public String toString() {
        return value;
    }
}
