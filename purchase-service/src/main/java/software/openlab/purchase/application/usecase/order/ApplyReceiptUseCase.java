package software.openlab.purchase.application.usecase.order;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Map;
import org.jboss.logging.Logger;
import software.openlab.purchase.domain.order.OrderEvents;
import software.openlab.purchase.domain.order.PurchaseOrder;
import software.openlab.purchase.domain.order.PurchaseOrderId;
import software.openlab.purchase.domain.order.PurchaseOrderRepository;
import software.openlab.purchase.domain.shared.DomainEvent;
import software.openlab.purchase.domain.shared.ProcessedEventRepository;

/** Reaction to a validated receipt from the inventory module. Idempotent per receipt. */
@ApplicationScoped
public class ApplyReceiptUseCase {

    private static final Logger LOG = Logger.getLogger(ApplyReceiptUseCase.class);

    @Inject
    PurchaseOrderRepository repository;

    @Inject
    ProcessedEventRepository processed;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public void execute(String receiptId, PurchaseOrderId orderId, Map<String, BigDecimal> receivedByProduct) {
        if (!processed.firstTime("picking.done:" + receiptId)) {
            return;
        }
        PurchaseOrder order = repository.findById(orderId).orElse(null);
        if (order == null) {
            LOG.warnf("receipt %s refers to unknown purchase order %s; ignoring", receiptId, orderId);
            return;
        }
        order.applyReceipt(receivedByProduct);
        repository.update(order);
        events.fire(new DomainEvent(OrderEvents.UPDATED, OrderEvents.OrderPayload.of(order)));
    }
}
