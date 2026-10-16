package software.openlab.sales.application.usecase.order;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.sales.domain.order.OrderCommand;
import software.openlab.sales.domain.order.OrderEvents;
import software.openlab.sales.domain.order.SalesOrder;
import software.openlab.sales.domain.order.SalesOrderRepository;
import software.openlab.sales.domain.shared.DomainEvent;

/** Creates a quotation (state DRAFT) with the next reference number. */
@ApplicationScoped
public class CreateSalesOrderUseCase {

    @Inject
    SalesOrderRepository repository;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public SalesOrder execute(OrderCommand raw) {
        SalesOrder order = SalesOrder.newQuotation(repository.nextNumber(), raw.validated());
        repository.insert(order);
        events.fire(new DomainEvent(OrderEvents.CREATED, OrderEvents.OrderPayload.of(order)));
        return order;
    }
}
