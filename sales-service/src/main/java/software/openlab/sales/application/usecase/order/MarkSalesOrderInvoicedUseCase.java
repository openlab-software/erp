package software.openlab.sales.application.usecase.order;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import software.openlab.sales.domain.order.OrderEvents;
import software.openlab.sales.domain.order.SalesOrder;
import software.openlab.sales.domain.order.SalesOrderId;
import software.openlab.sales.domain.order.SalesOrderRepository;
import software.openlab.sales.domain.shared.DomainEvent;
import software.openlab.sales.domain.shared.ProcessedEventRepository;

/** Reaction to a posted invoice from the accounting module. Idempotent per invoice. */
@ApplicationScoped
public class MarkSalesOrderInvoicedUseCase {

    private static final Logger LOG = Logger.getLogger(MarkSalesOrderInvoicedUseCase.class);

    @Inject
    SalesOrderRepository repository;

    @Inject
    ProcessedEventRepository processed;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public void execute(String invoiceId, SalesOrderId orderId) {
        if (!processed.firstTime("invoice.posted:" + invoiceId)) {
            return;
        }
        SalesOrder order = repository.findById(orderId).orElse(null);
        if (order == null) {
            LOG.warnf("invoice %s refers to unknown sales order %s; ignoring", invoiceId, orderId);
            return;
        }
        order.markInvoiced();
        repository.update(order);
        events.fire(new DomainEvent(OrderEvents.UPDATED, OrderEvents.OrderPayload.of(order)));
    }
}
