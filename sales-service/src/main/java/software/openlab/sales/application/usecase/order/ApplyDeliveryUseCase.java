package software.openlab.sales.application.usecase.order;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Map;
import org.jboss.logging.Logger;
import software.openlab.sales.domain.order.OrderEvents;
import software.openlab.sales.domain.order.SalesOrder;
import software.openlab.sales.domain.order.SalesOrderId;
import software.openlab.sales.domain.order.SalesOrderRepository;
import software.openlab.sales.domain.shared.DomainEvent;
import software.openlab.sales.domain.shared.ProcessedEventRepository;

/** Reaction to a validated delivery from the inventory module. Idempotent per delivery. */
@ApplicationScoped
public class ApplyDeliveryUseCase {

    private static final Logger LOG = Logger.getLogger(ApplyDeliveryUseCase.class);

    @Inject
    SalesOrderRepository repository;

    @Inject
    ProcessedEventRepository processed;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public void execute(String deliveryId, SalesOrderId orderId, Map<String, BigDecimal> deliveredByProduct) {
        if (!processed.firstTime("picking.done:" + deliveryId)) {
            return;
        }
        SalesOrder order = repository.findById(orderId).orElse(null);
        if (order == null) {
            LOG.warnf("delivery %s refers to unknown sales order %s; ignoring", deliveryId, orderId);
            return;
        }
        order.applyDelivery(deliveredByProduct);
        repository.update(order);
        events.fire(new DomainEvent(OrderEvents.UPDATED, OrderEvents.OrderPayload.of(order)));
    }
}
