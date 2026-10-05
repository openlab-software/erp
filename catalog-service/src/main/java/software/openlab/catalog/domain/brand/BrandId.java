package software.openlab.catalog.domain.brand;

import software.openlab.catalog.domain.shared.BadRequestException;
import software.openlab.catalog.domain.shared.Ulid;

/**
 * Strongly-typed public identifier for a {@link Brand}, in the form
 * {@code brand_<ULID>} (e.g. {@code brand_01J6Z3K9QZXJ6R6JYX8G0XJ0KQ}).
 */
public record BrandId(String value) {

    private static final String PREFIX = "brand";
    private static final String PREFIXED = PREFIX + "_";

    public BrandId {
        if (value == null || !value.startsWith(PREFIXED) || !Ulid.isValid(value.substring(PREFIXED.length()))) {
            throw new BadRequestException("error.id.invalid", PREFIXED + "<ULID>");
        }
    }

    public static BrandId generate() {
        return new BrandId(PREFIXED + Ulid.generate());
    }

    public static BrandId of(String raw) {
        return new BrandId(raw);
    }

    @Override
    public String toString() {
        return value;
    }
}
