package software.openlab.accounting.domain.invoice;

import software.openlab.accounting.domain.shared.BadRequestException;

public enum InvoiceState {
    DRAFT,
    POSTED,
    CANCELLED;

    public static InvoiceState parse(String raw) {
        try {
            return valueOf(raw.trim().toUpperCase());
        } catch (RuntimeException e) {
            throw new BadRequestException("error.field.invalid", "state", raw);
        }
    }
}
