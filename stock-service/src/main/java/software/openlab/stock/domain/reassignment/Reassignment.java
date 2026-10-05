package software.openlab.stock.domain.reassignment;

import java.time.Instant;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import software.openlab.stock.domain.stock.StockId;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Reassignment {

    private final ReassignmentId reassignmentId;
    private final StockId fromStockId;
    private final StockId toStockId;
    private final List<ReassignmentItem> items;
    private final Instant createdAt;

    public static Reassignment create(StockId fromStockId, StockId toStockId, List<ReassignmentItem> items) {
        return new Reassignment(ReassignmentId.generate(), fromStockId, toStockId, items, Instant.now());
    }
}
