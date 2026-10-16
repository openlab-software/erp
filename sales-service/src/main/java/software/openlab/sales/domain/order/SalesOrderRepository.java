package software.openlab.sales.domain.order;

import java.math.BigDecimal;
import java.util.Optional;
import software.openlab.sales.domain.shared.PageResult;

public interface SalesOrderRepository {

    Optional<SalesOrder> findById(SalesOrderId orderId);

    Optional<SalesOrder> findByNumber(String number);

    PageResult<SalesOrder> find(OrderFilter filter, int page, int pageSize);

    /** Next human reference (S00001, S00002, ...). */
    String nextNumber();

    void insert(SalesOrder order);

    void update(SalesOrder order);

    void deleteById(SalesOrderId orderId);

    /** Count and total amount per state, for the dashboard. */
    java.util.List<StateTotals> totalsByState();

    record StateTotals(OrderState state, long count, BigDecimal totalAmount) {
    }
}
