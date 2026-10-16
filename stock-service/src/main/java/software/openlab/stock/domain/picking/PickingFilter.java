package software.openlab.stock.domain.picking;

import software.openlab.stock.domain.stock.StockId;

/** Listing filters; every field may be null. q matches the number, the partner or the origin number. */
public record PickingFilter(String q, PickingType type, PickingState state, String originType, String originId,
                            StockId stockId) {
}
