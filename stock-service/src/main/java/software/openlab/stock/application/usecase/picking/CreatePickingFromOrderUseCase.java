package software.openlab.stock.application.usecase.picking;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.jboss.logging.Logger;
import software.openlab.stock.domain.picking.Picking;
import software.openlab.stock.domain.picking.PickingCommand;
import software.openlab.stock.domain.picking.PickingEvents;
import software.openlab.stock.domain.picking.PickingLine;
import software.openlab.stock.domain.picking.PickingRepository;
import software.openlab.stock.domain.picking.PickingType;
import software.openlab.stock.domain.shared.ApiException;
import software.openlab.stock.domain.shared.DomainEvent;
import software.openlab.stock.domain.shared.ProcessedEventRepository;
import software.openlab.stock.domain.shared.ProductId;
import software.openlab.stock.domain.stock.StockId;
import software.openlab.stock.domain.stockitem.StockItemRepository;

/**
 * Reaction to a confirmed sales order (a DELIVERY) or purchase order (a RECEIPT): drafts the matching picking,
 * already CONFIRMED so it shows up as ready to validate. Only goods are moved: lines whose product has no stock
 * item (services, unknown products) are skipped, and stock counts whole units, so a fractional quantity is
 * rounded up. Idempotent per originating document.
 */
@ApplicationScoped
public class CreatePickingFromOrderUseCase {

    private static final Logger LOG = Logger.getLogger(CreatePickingFromOrderUseCase.class);

    @Inject
    PickingRepository repository;

    @Inject
    StockItemRepository stockItemRepository;

    @Inject
    DefaultStockResolver defaultStock;

    @Inject
    ProcessedEventRepository processed;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public void execute(PickingType type, String originType, String originId, String originNumber,
                        String partnerId, String partnerName, List<OrderLine> orderLines) {
        if (!processed.firstTime(originType + ".confirmed:" + originId)
                || repository.existsByOrigin(type, originType, originId)) {
            return;
        }

        List<PickingLine> lines = new ArrayList<>();
        for (OrderLine ol : orderLines) {
            ProductId productId;
            try {
                productId = ProductId.of(ol.productId());
            } catch (ApiException e) {
                LOG.warnf("%s %s: ignoring line with invalid product id '%s'", originType, originNumber,
                        ol.productId());
                continue;
            }
            int quantity = ol.quantity().setScale(0, RoundingMode.CEILING).intValue();
            if (quantity <= 0 || "SERVICE".equals(ol.productType())
                    || stockItemRepository.findAllByProduct(productId).isEmpty()) {
                continue;
            }
            lines.add(new PickingLine(productId, ol.description(), quantity, 0));
        }
        if (lines.isEmpty()) {
            LOG.infof("%s %s has no stockable lines; no %s created", originType, originNumber, type);
            return;
        }

        StockId stock = defaultStock.resolve().orElse(null);
        PickingCommand command = new PickingCommand(
                type == PickingType.RECEIPT ? null : stock,
                type == PickingType.DELIVERY ? null : stock,
                partnerId, partnerName, null, null, lines).validated(type);

        Picking picking = Picking.newPicking(repository.nextNumber(type), type, command);
        picking.setOrigin(originType, originId, originNumber);
        picking.confirm();
        repository.insert(picking);

        events.fire(new DomainEvent(PickingEvents.CREATED, PickingEvents.PickingPayload.of(picking)));
        events.fire(new DomainEvent(PickingEvents.CONFIRMED, PickingEvents.PickingPayload.of(picking)));
    }

    /** {@code productType} is GOOD or SERVICE as snapshotted by the order; services are never moved. */
    public record OrderLine(String productId, String description, BigDecimal quantity, String productType) {
    }
}
