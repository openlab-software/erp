package software.openlab.purchase.domain.order;

import software.openlab.purchase.domain.shared.BadRequestException;

/** Billing progress, driven by the accounting module (when it is installed). */
public enum BillStatus {
    NONE,
    TO_BILL,
    BILLED;

    public static BillStatus parse(String raw) {
        try {
            return valueOf(raw.trim().toUpperCase());
        } catch (RuntimeException e) {
            throw new BadRequestException("error.field.invalid", "bill_status", raw);
        }
    }
}
