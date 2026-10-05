package software.openlab.catalog.domain.unitofmeasure;

import software.openlab.catalog.domain.shared.BadRequestException;
import software.openlab.catalog.domain.shared.Ulid;

/**
 * Strongly-typed public identifier for a {@link UnitOfMeasure}, in the form
 * {@code uom_<ULID>} (e.g. {@code uom_01J6Z3K9QZXJ6R6JYX8G0XJ0KQ}).
 */
public record UnitOfMeasureId(String value) {

    private static final String PREFIX = "uom";
    private static final String PREFIXED = PREFIX + "_";

    public UnitOfMeasureId {
        if (value == null || !value.startsWith(PREFIXED) || !Ulid.isValid(value.substring(PREFIXED.length()))) {
            throw new BadRequestException("error.id.invalid", PREFIXED + "<ULID>");
        }
    }

    public static UnitOfMeasureId generate() {
        return new UnitOfMeasureId(PREFIXED + Ulid.generate());
    }

    public static UnitOfMeasureId of(String raw) {
        return new UnitOfMeasureId(raw);
    }

    @Override
    public String toString() {
        return value;
    }
}
