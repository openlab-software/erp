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
public class LockSalesOrderUseCase {

    @Inject
    SalesOrderRepository repository;

    @Inject
    GetSalesOrderByIdUseCase getOrderById;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public SalesOrder execute(SalesOrderId orderId) {
        SalesOrder order = getOrderById.execute(orderId);
        order.lock();
        repository.update(order);
        SalesOrder updated = getOrderById.execute(orderId);
        events.fire(new DomainEvent(OrderEvents.UPDATED, OrderEvents.OrderPayload.of(updated)));
        return updated;
    }
}
