package software.openlab.sales.domain.order;

import software.openlab.sales.domain.shared.BadRequestException;

/** Billing progress, driven by the accounting module (when it is installed). */
public enum InvoiceStatus {
    NONE,
    TO_INVOICE,
    INVOICED;

    public static InvoiceStatus parse(String raw) {
        try {
            return valueOf(raw.trim().toUpperCase());
        } catch (RuntimeException e) {
            throw new BadRequestException("error.field.invalid", "invoice_status", raw);
        }
    }
}
