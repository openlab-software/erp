package software.openlab.catalog.domain.supplier;

import software.openlab.catalog.domain.shared.BadRequestException;
import software.openlab.catalog.domain.shared.Ulid;

/**
 * Strongly-typed public identifier for a {@link Supplier}, in the form
 * {@code supplier_<ULID>} (e.g. {@code supplier_01J6Z3K9QZXJ6R6JYX8G0XJ0KQ}).
 */
public record SupplierId(String value) {

    private static final String PREFIX = "supplier";
    private static final String PREFIXED = PREFIX + "_";

    public SupplierId {
        if (value == null || !value.startsWith(PREFIXED) || !Ulid.isValid(value.substring(PREFIXED.length()))) {
            throw new BadRequestException("error.id.invalid", PREFIXED + "<ULID>");
        }
    }

    public static SupplierId generate() {
        return new SupplierId(PREFIXED + Ulid.generate());
    }

    public static SupplierId of(String raw) {
        return new SupplierId(raw);
    }

    @Override
    public String toString() {
        return value;
    }
}
