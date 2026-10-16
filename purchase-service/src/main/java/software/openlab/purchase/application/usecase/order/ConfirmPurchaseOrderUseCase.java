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

/**
 * Confirms the RFQ. The {@code purchase_order.confirmed} event carries the full order, which is what the
 * inventory module (receipt) and the accounting module (invoice) react to when they are installed.
 */
@ApplicationScoped
public class ConfirmPurchaseOrderUseCase {

    @Inject
    PurchaseOrderRepository repository;

    @Inject
    GetPurchaseOrderByIdUseCase getOrderById;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public PurchaseOrder execute(PurchaseOrderId orderId) {
        PurchaseOrder order = getOrderById.execute(orderId);
        order.confirm();
        repository.update(order);
        PurchaseOrder confirmed = getOrderById.execute(orderId);
        events.fire(new DomainEvent(OrderEvents.CONFIRMED, OrderEvents.OrderConfirmedPayload.of(confirmed)));
        return confirmed;
    }
}
