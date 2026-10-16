package software.openlab.purchase.domain.order;

import software.openlab.purchase.domain.shared.BadRequestException;

/** Fulfilment progress, driven by the inventory module (when it is installed). */
public enum ReceiptStatus {
    NONE,
    PENDING,
    PARTIAL,
    DONE;

    public static ReceiptStatus parse(String raw) {
        try {
            return valueOf(raw.trim().toUpperCase());
        } catch (RuntimeException e) {
            throw new BadRequestException("error.field.invalid", "receipt_status", raw);
        }
    }
}
