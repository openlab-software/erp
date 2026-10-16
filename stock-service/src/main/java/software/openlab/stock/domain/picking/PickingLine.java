package software.openlab.stock.domain.picking;

import software.openlab.stock.domain.shared.ProductId;

/** {@code demand} is what must move; {@code done} what actually moved (0 until the picking is validated). */
public record PickingLine(ProductId productId, String description, int demand, int done) {

    public int remaining() {
        return Math.max(demand - done, 0);
    }
}
