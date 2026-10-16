package software.openlab.accounting.domain.invoice;

import software.openlab.accounting.domain.shared.BadRequestException;

public enum PaymentState {
    NOT_PAID,
    PARTIAL,
    PAID;

    public static PaymentState parse(String raw) {
        try {
            return valueOf(raw.trim().toUpperCase());
        } catch (RuntimeException e) {
            throw new BadRequestException("error.field.invalid", "payment_state", raw);
        }
    }
}
