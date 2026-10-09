package software.openlab.customer.domain.customer;

import software.openlab.customer.domain.shared.BadRequestException;
import software.openlab.customer.domain.shared.Ulid;

/**
 * Strongly-typed public identifier for a {@link Customer}, in the form
 * {@code customer_<ULID>} (e.g. {@code customer_01J6Z3K9QZXJ6R6JYX8G0XJ0KQ}).
 */
public record CustomerId(String value) {

    private static final String PREFIX = "customer";
    private static final String PREFIXED = PREFIX + "_";

    public CustomerId {
        if (value == null || !value.startsWith(PREFIXED) || !Ulid.isValid(value.substring(PREFIXED.length()))) {
            throw new BadRequestException("error.id.invalid", PREFIXED + "<ULID>");
        }
    }

    public static CustomerId generate() {
        return new CustomerId(PREFIXED + Ulid.generate());
    }

    public static CustomerId of(String raw) {
        return new CustomerId(raw);
    }

    @Override
    public String toString() {
        return value;
    }
}
