package software.openlab.sales.application.usecase.order;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.sales.domain.order.OrderEvents;
import software.openlab.sales.domain.order.SalesOrder;
import software.openlab.sales.domain.order.SalesOrderId;
import software.openlab.sales.domain.order.SalesOrderRepository;
import software.openlab.sales.domain.shared.DomainEvent;

/**
 * Confirms the quotation. The {@code sales_order.confirmed} event carries the full order, which is what the
 * inventory module (delivery) and the accounting module (invoice) react to when they are installed.
 */
@ApplicationScoped
public class ConfirmSalesOrderUseCase {

    @Inject
    SalesOrderRepository repository;

    @Inject
    GetSalesOrderByIdUseCase getOrderById;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public SalesOrder execute(SalesOrderId orderId) {
        SalesOrder order = getOrderById.execute(orderId);
        order.confirm();
        repository.update(order);
        SalesOrder confirmed = getOrderById.execute(orderId);
        events.fire(new DomainEvent(OrderEvents.CONFIRMED, OrderEvents.OrderConfirmedPayload.of(confirmed)));
        return confirmed;
    }
}
