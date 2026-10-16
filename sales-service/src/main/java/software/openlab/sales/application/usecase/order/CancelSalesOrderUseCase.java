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

@ApplicationScoped
public class CancelSalesOrderUseCase {

    @Inject
    SalesOrderRepository repository;

    @Inject
    GetSalesOrderByIdUseCase getOrderById;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public SalesOrder execute(SalesOrderId orderId) {
        SalesOrder order = getOrderById.execute(orderId);
        order.cancel();
        repository.update(order);
        SalesOrder cancelled = getOrderById.execute(orderId);
        events.fire(new DomainEvent(OrderEvents.CANCELLED, OrderEvents.OrderPayload.of(cancelled)));
        return cancelled;
    }
}
