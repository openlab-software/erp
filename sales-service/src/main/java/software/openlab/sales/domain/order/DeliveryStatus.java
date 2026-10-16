package software.openlab.sales.domain.order;

import software.openlab.sales.domain.shared.BadRequestException;

/** Fulfilment progress, driven by the inventory module (when it is installed). */
public enum DeliveryStatus {
    NONE,
    PENDING,
    PARTIAL,
    DONE;

    public static DeliveryStatus parse(String raw) {
        try {
            return valueOf(raw.trim().toUpperCase());
        } catch (RuntimeException e) {
            throw new BadRequestException("error.field.invalid", "delivery_status", raw);
        }
    }
}
