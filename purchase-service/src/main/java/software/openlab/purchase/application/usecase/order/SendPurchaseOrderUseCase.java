package software.openlab.purchase.application.usecase.order;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.purchase.domain.order.OrderEvents;
import software.openlab.purchase.domain.order.PurchaseOrder;
import software.openlab.purchase.domain.order.PurchaseOrderId;
import software.openlab.purchase.domain.order.PurchaseOrderRepository;
import software.openlab.purchase.domain.shared.DomainEvent;

/** Marks the RFQ as sent to the supplier. */
@ApplicationScoped
public class SendPurchaseOrderUseCase {

    @Inject
    PurchaseOrderRepository repository;

    @Inject
    GetPurchaseOrderByIdUseCase getOrderById;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public PurchaseOrder execute(PurchaseOrderId orderId) {
        PurchaseOrder order = getOrderById.execute(orderId);
        order.markSent();
        repository.update(order);
        PurchaseOrder updated = getOrderById.execute(orderId);
        events.fire(new DomainEvent(OrderEvents.UPDATED, OrderEvents.OrderPayload.of(updated)));
        return updated;
    }
}
