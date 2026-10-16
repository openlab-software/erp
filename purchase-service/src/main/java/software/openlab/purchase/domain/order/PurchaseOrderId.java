package software.openlab.purchase.domain.order;

import software.openlab.purchase.domain.shared.BadRequestException;
import software.openlab.purchase.domain.shared.Ulid;

/** Public identifier: {@code purchase_order_<ULID>}. */
public record PurchaseOrderId(String value) {

    private static final String PREFIXED = "purchase_order_";

    public PurchaseOrderId {
        if (value == null || !value.startsWith(PREFIXED) || !Ulid.isValid(value.substring(PREFIXED.length()))) {
            throw new BadRequestException("error.id.invalid", PREFIXED + "<ULID>");
        }
    }

    public static PurchaseOrderId generate() {
        return new PurchaseOrderId(PREFIXED + Ulid.generate());
    }

    public static PurchaseOrderId of(String raw) {
        return new PurchaseOrderId(raw);
    }

    @Override
    public String toString() {
        return value;
    }
}
