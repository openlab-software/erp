package software.openlab.purchase.domain.order;

import java.math.BigDecimal;
import java.util.Optional;
import software.openlab.purchase.domain.shared.PageResult;

public interface PurchaseOrderRepository {

    Optional<PurchaseOrder> findById(PurchaseOrderId orderId);

    Optional<PurchaseOrder> findByNumber(String number);

    PageResult<PurchaseOrder> find(OrderFilter filter, int page, int pageSize);

    /** Next human reference (S00001, S00002, ...). */
    String nextNumber();

    void insert(PurchaseOrder order);

    void update(PurchaseOrder order);

    void deleteById(PurchaseOrderId orderId);

    /** Count and total amount per state, for the dashboard. */
    java.util.List<StateTotals> totalsByState();

    record StateTotals(OrderState state, long count, BigDecimal totalAmount) {
    }
}
