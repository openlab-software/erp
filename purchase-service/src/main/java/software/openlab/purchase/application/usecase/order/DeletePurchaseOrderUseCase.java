package software.openlab.purchase.application.usecase.order;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.purchase.domain.order.OrderEvents;
import software.openlab.purchase.domain.order.OrderState;
import software.openlab.purchase.domain.order.PurchaseOrder;
import software.openlab.purchase.domain.order.PurchaseOrderId;
import software.openlab.purchase.domain.order.PurchaseOrderRepository;
import software.openlab.purchase.domain.shared.ConflictException;
import software.openlab.purchase.domain.shared.DomainEvent;

/** Only RFQs that never became orders (DRAFT) or were cancelled can be deleted. */
@ApplicationScoped
public class DeletePurchaseOrderUseCase {

    @Inject
    PurchaseOrderRepository repository;

    @Inject
    GetPurchaseOrderByIdUseCase getOrderById;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public void execute(PurchaseOrderId orderId) {
        PurchaseOrder order = getOrderById.execute(orderId);
        if (order.getState() != OrderState.DRAFT && order.getState() != OrderState.CANCELLED) {
            throw new ConflictException("error.order.notDeletable", order.getState());
        }
        repository.deleteById(orderId);
        events.fire(new DomainEvent(OrderEvents.DELETED, new OrderEvents.OrderDeletedPayload(orderId.toString())));
    }
}
