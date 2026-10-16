package software.openlab.accounting.domain.invoice;

import software.openlab.accounting.domain.shared.BadRequestException;
import software.openlab.accounting.domain.shared.Ulid;

/** Public identifier: {@code invoice_<ULID>}. */
public record InvoiceId(String value) {

    private static final String PREFIXED = "invoice_";

    public InvoiceId {
        if (value == null || !value.startsWith(PREFIXED) || !Ulid.isValid(value.substring(PREFIXED.length()))) {
            throw new BadRequestException("error.id.invalid", PREFIXED + "<ULID>");
        }
    }

    public static InvoiceId generate() {
        return new InvoiceId(PREFIXED + Ulid.generate());
    }

    public static InvoiceId of(String raw) {
        return new InvoiceId(raw);
    }

    @Override
    public String toString() {
        return value;
    }
}
