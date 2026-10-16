package software.openlab.purchase.application.usecase.order;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.purchase.domain.order.OrderCommand;
import software.openlab.purchase.domain.order.OrderEvents;
import software.openlab.purchase.domain.order.PurchaseOrder;
import software.openlab.purchase.domain.order.PurchaseOrderRepository;
import software.openlab.purchase.domain.shared.DomainEvent;

/** Creates a RFQ (state DRAFT) with the next reference number. */
@ApplicationScoped
public class CreatePurchaseOrderUseCase {

    @Inject
    PurchaseOrderRepository repository;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public PurchaseOrder execute(OrderCommand raw) {
        PurchaseOrder order = PurchaseOrder.newRFQ(repository.nextNumber(), raw.validated());
        repository.insert(order);
        events.fire(new DomainEvent(OrderEvents.CREATED, OrderEvents.OrderPayload.of(order)));
        return order;
    }
}
