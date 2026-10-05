package software.openlab.catalog.domain.product;

import software.openlab.catalog.domain.shared.BadRequestException;
import software.openlab.catalog.domain.shared.Ulid;

/**
 * Strongly-typed public identifier for a {@link ProductImage}, in the form
 * {@code image_<ULID>} (e.g. {@code image_01J6Z3K9QZXJ6R6JYX8G0XJ0KQ}).
 */
public record ProductImageId(String value) {

    private static final String PREFIX = "image";
    private static final String PREFIXED = PREFIX + "_";

    public ProductImageId {
        if (value == null || !value.startsWith(PREFIXED) || !Ulid.isValid(value.substring(PREFIXED.length()))) {
            throw new BadRequestException("error.id.invalid", PREFIXED + "<ULID>");
        }
    }

    public static ProductImageId generate() {
        return new ProductImageId(PREFIXED + Ulid.generate());
    }

    public static ProductImageId of(String raw) {
        return new ProductImageId(raw);
    }

    @Override
    public String toString() {
        return value;
    }
}
