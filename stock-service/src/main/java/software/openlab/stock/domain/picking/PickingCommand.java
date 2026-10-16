package software.openlab.stock.domain.picking;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import software.openlab.stock.domain.shared.BadRequestException;
import software.openlab.stock.domain.shared.ProductId;
import software.openlab.stock.domain.stock.StockId;

/**
 * Editable data of a picking. Only the stocks that make sense for the type are kept (a delivery has no
 * destination stock, a receipt no source); lines of the same product are merged.
 */
public record PickingCommand(
        StockId sourceStockId,
        StockId destinationStockId,
        String partnerId,
        String partnerName,
        LocalDate scheduledDate,
        String note,
        List<PickingLine> lines) {

    public static final int MAX_LINES = 200;

    public PickingCommand validated(PickingType type) {
        StockId source = type == PickingType.RECEIPT ? null : sourceStockId;
        StockId destination = type == PickingType.DELIVERY ? null : destinationStockId;
        if (type == PickingType.INTERNAL && source != null && source.equals(destination)) {
            throw new BadRequestException("error.picking.sameStock");
        }

        Map<ProductId, PickingLine> merged = new LinkedHashMap<>();
        for (PickingLine line : lines == null ? List.<PickingLine>of() : lines) {
            if (line.demand() <= 0) {
                throw new BadRequestException("error.picking.demandPositive", line.productId());
            }
            merged.merge(line.productId(), new PickingLine(line.productId(), line.description(), line.demand(), 0),
                    (a, b) -> new PickingLine(a.productId(), a.description() != null ? a.description()
                            : b.description(), a.demand() + b.demand(), 0));
        }
        if (merged.size() > MAX_LINES) {
            throw new BadRequestException("error.picking.line.tooMany", MAX_LINES);
        }
        return new PickingCommand(source, destination, blankToNull(partnerId), blankToNull(partnerName),
                scheduledDate, blankToNull(note), new ArrayList<>(merged.values()));
    }

    private static String blankToNull(String v) {
        return v == null || v.isBlank() ? null : v.trim();
    }
}
