package software.openlab.purchase.application.usecase.order;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import software.openlab.purchase.domain.order.OrderEvents;
import software.openlab.purchase.domain.order.PurchaseOrder;
import software.openlab.purchase.domain.order.PurchaseOrderId;
import software.openlab.purchase.domain.order.PurchaseOrderRepository;
import software.openlab.purchase.domain.shared.DomainEvent;
import software.openlab.purchase.domain.shared.ProcessedEventRepository;

/** Reaction to a posted invoice from the accounting module. Idempotent per invoice. */
@ApplicationScoped
public class MarkPurchaseOrderBilledUseCase {

    private static final Logger LOG = Logger.getLogger(MarkPurchaseOrderBilledUseCase.class);

    @Inject
    PurchaseOrderRepository repository;

    @Inject
    ProcessedEventRepository processed;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public void execute(String billId, PurchaseOrderId orderId) {
        if (!processed.firstTime("invoice.posted:" + billId)) {
            return;
        }
        PurchaseOrder order = repository.findById(orderId).orElse(null);
        if (order == null) {
            LOG.warnf("invoice %s refers to unknown purchase order %s; ignoring", billId, orderId);
            return;
        }
        order.markBilled();
        repository.update(order);
        events.fire(new DomainEvent(OrderEvents.UPDATED, OrderEvents.OrderPayload.of(order)));
    }
}
