package software.openlab.sales.application.usecase.order;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.sales.domain.order.OrderEvents;
import software.openlab.sales.domain.order.OrderState;
import software.openlab.sales.domain.order.SalesOrder;
import software.openlab.sales.domain.order.SalesOrderId;
import software.openlab.sales.domain.order.SalesOrderRepository;
import software.openlab.sales.domain.shared.ConflictException;
import software.openlab.sales.domain.shared.DomainEvent;

/** Only quotations that never became orders (DRAFT) or were cancelled can be deleted. */
@ApplicationScoped
public class DeleteSalesOrderUseCase {

    @Inject
    SalesOrderRepository repository;

    @Inject
    GetSalesOrderByIdUseCase getOrderById;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public void execute(SalesOrderId orderId) {
        SalesOrder order = getOrderById.execute(orderId);
        if (order.getState() != OrderState.DRAFT && order.getState() != OrderState.CANCELLED) {
            throw new ConflictException("error.order.notDeletable", order.getState());
        }
        repository.deleteById(orderId);
        events.fire(new DomainEvent(OrderEvents.DELETED, new OrderEvents.OrderDeletedPayload(orderId.toString())));
    }
}
