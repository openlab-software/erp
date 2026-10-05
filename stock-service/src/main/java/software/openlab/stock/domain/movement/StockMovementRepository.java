package software.openlab.stock.domain.movement;

import java.time.Instant;
import software.openlab.stock.domain.movementreason.MovementReasonId;
import software.openlab.stock.domain.shared.PageResult;
import software.openlab.stock.domain.shared.ProductId;
import software.openlab.stock.domain.stock.StockId;

public interface StockMovementRepository {

    void insert(StockMovement movement);

    /**
     * @param productId optional filter, only movements of this product when present
     * @param type      optional filter
     * @param from      optional filter, {@code createdAt >= from}
     * @param to        optional filter, {@code createdAt <= to}
     */
    PageResult<StockMovement> findByStock(StockId stockId, ProductId productId, StockMovementType type,
                                           Instant from, Instant to, int page, int pageSize);

    /** True if any StockMovement references this Movement_Reason (Requirement 2.4). */
    boolean existsByReasonId(MovementReasonId reasonId);
}
