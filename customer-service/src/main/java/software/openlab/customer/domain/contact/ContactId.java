package software.openlab.customer.domain.contact;

import software.openlab.customer.domain.shared.BadRequestException;
import software.openlab.customer.domain.shared.Ulid;

/** Public identifier of a {@link Contact}: {@code contact_<ULID>}. */
public record ContactId(String value) {

    private static final String PREFIXED = "contact_";

    public ContactId {
        if (value == null || !value.startsWith(PREFIXED) || !Ulid.isValid(value.substring(PREFIXED.length()))) {
            throw new BadRequestException("error.id.invalid", PREFIXED + "<ULID>");
        }
    }

    public static ContactId generate() {
        return new ContactId(PREFIXED + Ulid.generate());
    }

    public static ContactId of(String raw) {
        return new ContactId(raw);
    }

    @Override
    public String toString() {
        return value;
    }
}
