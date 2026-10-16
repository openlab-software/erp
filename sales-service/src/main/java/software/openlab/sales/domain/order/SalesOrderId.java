package software.openlab.sales.domain.order;

import software.openlab.sales.domain.shared.BadRequestException;
import software.openlab.sales.domain.shared.Ulid;

/** Public identifier: {@code sales_order_<ULID>}. */
public record SalesOrderId(String value) {

    private static final String PREFIXED = "sales_order_";

    public SalesOrderId {
        if (value == null || !value.startsWith(PREFIXED) || !Ulid.isValid(value.substring(PREFIXED.length()))) {
            throw new BadRequestException("error.id.invalid", PREFIXED + "<ULID>");
        }
    }

    public static SalesOrderId generate() {
        return new SalesOrderId(PREFIXED + Ulid.generate());
    }

    public static SalesOrderId of(String raw) {
        return new SalesOrderId(raw);
    }

    @Override
    public String toString() {
        return value;
    }
}
