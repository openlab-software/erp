package software.openlab.purchase.domain.order;

import software.openlab.purchase.domain.shared.BadRequestException;

/** DRAFT = RFQ being prepared, SENT = RFQ sent to the supplier, CONFIRMED = purchase order. */
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
