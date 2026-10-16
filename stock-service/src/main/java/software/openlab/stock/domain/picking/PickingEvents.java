package software.openlab.stock.domain.picking;

import java.util.List;

public final class PickingEvents {

    public static final String CREATED = "picking.created";
    public static final String UPDATED = "picking.updated";
    public static final String CONFIRMED = "picking.confirmed";
    public static final String DONE = "picking.done";
    public static final String CANCELLED = "picking.cancelled";
    public static final String DELETED = "picking.deleted";

    private PickingEvents() {
    }

    /**
     * {@code lines[].quantity} is the quantity moved when the picking is DONE and the demand otherwise.
     * {@code originType/originId} let the originating module (sales, purchase) match its own order when it
     * sees {@code picking.done}.
     */
    public record PickingPayload(String id, String number, String type, String state, String originType,
                                 String originId, String originNumber, String sourceStockId,
                                 String destinationStockId, String backorderOf, List<Line> lines) {
        public static PickingPayload of(Picking p) {
            boolean done = p.getState() == PickingState.DONE;
            return new PickingPayload(p.getPickingId().toString(), p.getNumber(), p.getType().name(),
                    p.getState().name(), p.getOriginType(), p.getOriginId(), p.getOriginNumber(),
                    p.getSourceStockId() == null ? null : p.getSourceStockId().toString(),
                    p.getDestinationStockId() == null ? null : p.getDestinationStockId().toString(),
                    p.getBackorderOf() == null ? null : p.getBackorderOf().toString(),
                    p.getLines().stream()
                            .map(l -> new Line(l.productId().toString(), l.description(), l.demand(),
                                    done ? l.done() : l.demand()))
                            .toList());
        }
    }

    public record Line(String productId, String description, int demand, int quantity) {
    }

    public record PickingDeletedPayload(String id) {
    }
}
