package software.openlab.accounting.domain.invoice;

import software.openlab.accounting.domain.shared.BadRequestException;
import software.openlab.accounting.domain.shared.Ulid;

/** Public identifier: {@code payment_<ULID>}. */
public record PaymentId(String value) {

    private static final String PREFIXED = "payment_";

    public PaymentId {
        if (value == null || !value.startsWith(PREFIXED) || !Ulid.isValid(value.substring(PREFIXED.length()))) {
            throw new BadRequestException("error.id.invalid", PREFIXED + "<ULID>");
        }
    }

    public static PaymentId generate() {
        return new PaymentId(PREFIXED + Ulid.generate());
    }

    public static PaymentId of(String raw) {
        return new PaymentId(raw);
    }

    @Override
    public String toString() {
        return value;
    }
}
