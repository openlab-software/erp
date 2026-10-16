package software.openlab.accounting.domain.invoice;

import software.openlab.accounting.domain.shared.BadRequestException;

public enum PaymentMethod {
    CASH,
    PIX,
    BANK_TRANSFER,
    BOLETO,
    CREDIT_CARD,
    OTHER;

    public static PaymentMethod parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return OTHER;
        }
        try {
            return valueOf(raw.trim().toUpperCase());
        } catch (RuntimeException e) {
            throw new BadRequestException("error.field.invalid", "method", raw);
        }
    }
}
