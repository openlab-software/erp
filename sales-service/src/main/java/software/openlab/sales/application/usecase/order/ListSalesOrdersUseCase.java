package software.openlab.sales.application.usecase.order;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.openlab.sales.domain.order.DeliveryStatus;
import software.openlab.sales.domain.order.InvoiceStatus;
import software.openlab.sales.domain.order.OrderFilter;
import software.openlab.sales.domain.order.OrderState;
import software.openlab.sales.domain.order.SalesOrder;
import software.openlab.sales.domain.order.SalesOrderRepository;
import software.openlab.sales.domain.shared.PageResult;
import software.openlab.sales.domain.shared.Refs;

@ApplicationScoped
public class ListSalesOrdersUseCase {

    @Inject
    SalesOrderRepository repository;

    public PageResult<SalesOrder> execute(String q, String state, String customerId, String deliveryStatus,
                                          String invoiceStatus, Integer page, Integer pageSize) {
        OrderFilter filter = new OrderFilter(
                q,
                isBlank(state) ? null : OrderState.parse(state),
                Refs.optional("customer", customerId),
                isBlank(deliveryStatus) ? null : DeliveryStatus.parse(deliveryStatus),
                isBlank(invoiceStatus) ? null : InvoiceStatus.parse(invoiceStatus));
        return repository.find(filter, PageResult.normalizePage(page), PageResult.normalizePageSize(pageSize));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
