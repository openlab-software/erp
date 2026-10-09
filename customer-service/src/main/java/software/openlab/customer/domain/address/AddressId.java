package software.openlab.customer.domain.address;

import software.openlab.customer.domain.shared.BadRequestException;
import software.openlab.customer.domain.shared.Ulid;

/** Public identifier of an {@link Address}: {@code address_<ULID>}. */
public record AddressId(String value) {

    private static final String PREFIXED = "address_";

    public AddressId {
        if (value == null || !value.startsWith(PREFIXED) || !Ulid.isValid(value.substring(PREFIXED.length()))) {
            throw new BadRequestException("error.id.invalid", PREFIXED + "<ULID>");
        }
    }

    public static AddressId generate() {
        return new AddressId(PREFIXED + Ulid.generate());
    }

    public static AddressId of(String raw) {
        return new AddressId(raw);
    }

    @Override
    public String toString() {
        return value;
    }
}
