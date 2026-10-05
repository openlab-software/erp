package software.openlab.catalog.domain.category;

import software.openlab.catalog.domain.shared.BadRequestException;
import software.openlab.catalog.domain.shared.Ulid;

/**
 * Strongly-typed public identifier for a {@link Category}, in the form
 * {@code cat_<ULID>} (e.g. {@code cat_01J6Z3K9QZXJ6R6JYX8G0XJ0KQ}).
 */
public record CategoryId(String value) {

    private static final String PREFIX = "cat";
    private static final String PREFIXED = PREFIX + "_";

    public CategoryId {
        if (value == null || !value.startsWith(PREFIXED) || !Ulid.isValid(value.substring(PREFIXED.length()))) {
            throw new BadRequestException("error.id.invalid", PREFIXED + "<ULID>");
        }
    }

    public static CategoryId generate() {
        return new CategoryId(PREFIXED + Ulid.generate());
    }

    public static CategoryId of(String raw) {
        return new CategoryId(raw);
    }

    @Override
    public String toString() {
        return value;
    }
}
