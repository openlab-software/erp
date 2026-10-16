package software.openlab.sales.domain.order;

import software.openlab.sales.domain.shared.BadRequestException;

/** DRAFT = quotation being prepared, SENT = quotation sent to the customer, CONFIRMED = sales order. */
public enum OrderState {
    DRAFT,
    SENT,
    CONFIRMED,
    DONE,
    CANCELLED;

    public static OrderState parse(String raw) {
        try {
            return valueOf(raw.trim().toUpperCase());
        } catch (RuntimeException e) {
            throw new BadRequestException("error.field.invalid", "state", raw);
        }
    }
}
