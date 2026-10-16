package software.openlab.stock.domain.picking;

import software.openlab.stock.domain.shared.BadRequestException;

/**
 * RECEIPT brings goods into a stock (from a supplier), DELIVERY takes them out (to a customer) and INTERNAL
 * moves them between two stocks.
 */
public enum PickingType {
    RECEIPT("IN"),
    DELIVERY("OUT"),
    INTERNAL("INT");

    private final String numberPrefix;

    PickingType(String numberPrefix) {
        this.numberPrefix = numberPrefix;
    }

    public String numberPrefix() {
        return numberPrefix;
    }

    public static PickingType parse(String raw) {
        try {
            return valueOf(raw.trim().toUpperCase());
        } catch (RuntimeException e) {
            throw new BadRequestException("error.picking.typeInvalid");
        }
    }
}
