package software.openlab.stock.domain.picking;

import software.openlab.stock.domain.shared.PrefixedId;

/** Strongly-typed Picking (stock transfer) id, format {@code picking_<ULID>}. */
public record PickingId(String value) {

    public static final String PREFIX = "picking";

    public PickingId {
        value = PrefixedId.parse(PREFIX, value);
    }

    public static PickingId generate() {
        return new PickingId(PrefixedId.generate(PREFIX));
    }

    public static PickingId of(String raw) {
        return new PickingId(raw);
    }

    @Override
    public String toString() {
        return value;
    }
}
