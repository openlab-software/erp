package software.openlab.stock.application.usecase.picking;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import software.openlab.stock.application.usecase.movement.ApplyStockMovementUseCase;
import software.openlab.stock.domain.movement.StockMovementType;
import software.openlab.stock.domain.picking.Picking;
import software.openlab.stock.domain.picking.PickingCommand;
import software.openlab.stock.domain.picking.PickingEvents;
import software.openlab.stock.domain.picking.PickingId;
import software.openlab.stock.domain.picking.PickingLine;
import software.openlab.stock.domain.picking.PickingRepository;
import software.openlab.stock.domain.picking.PickingType;
import software.openlab.stock.domain.shared.DomainEvent;
import software.openlab.stock.domain.shared.NotFoundException;
import software.openlab.stock.domain.shared.ProductId;
import software.openlab.stock.domain.stock.StockId;
import software.openlab.stock.domain.stockitem.StockItem;
import software.openlab.stock.domain.stockitem.StockItemRepository;

/**
 * "Validate" of a picking: moves the goods through the regular stock movements (so balances, history and
 * {@code stock_movement.created} events behave like any manual movement) and closes the picking. A partial
 * validation can leave the remainder in a backorder; either way {@code picking.done} carries what moved, which
 * is what the sales / purchase modules use to track delivery and receipt.
 */
@ApplicationScoped
public class ValidatePickingUseCase {

    @Inject
    PickingRepository repository;

    @Inject
    GetPickingByIdUseCase getPickingById;

    @Inject
    StockItemRepository stockItemRepository;

    @Inject
    ApplyStockMovementUseCase applyStockMovement;

    @Inject
    Event<DomainEvent> events;

    /** @param doneByProduct quantities actually moved; a product that is absent moves its whole demand. */
    @Transactional
    public Result execute(PickingId pickingId, Map<ProductId, Integer> doneByProduct, boolean createBackorder) {
        Picking picking = getPickingById.execute(pickingId);
        picking.requireStocks();

        List<PickingLine> pending = picking.complete(doneByProduct);
        for (PickingLine line : picking.getLines()) {
            if (line.done() > 0) {
                move(picking, line.productId(), line.done());
            }
        }
        repository.update(picking);

        Picking backorder = null;
        if (createBackorder && !pending.isEmpty()) {
            backorder = Picking.newPicking(repository.nextNumber(picking.getType()), picking.getType(),
                    new PickingCommand(picking.getSourceStockId(), picking.getDestinationStockId(),
                            picking.getPartnerId(), picking.getPartnerName(), picking.getScheduledDate(),
                            picking.getNote(), pending));
            backorder.setOrigin(picking.getOriginType(), picking.getOriginId(), picking.getOriginNumber());
            backorder.setBackorderOf(picking.getPickingId());
            backorder.confirm();
            repository.insert(backorder);
        }

        Picking done = getPickingById.execute(pickingId);
        events.fire(new DomainEvent(PickingEvents.DONE, PickingEvents.PickingPayload.of(done)));
        if (backorder != null) {
            events.fire(new DomainEvent(PickingEvents.CREATED, PickingEvents.PickingPayload.of(backorder)));
        }
        return new Result(done, backorder);
    }

    private void move(Picking picking, ProductId productId, int quantity) {
        String reference = picking.getNumber();
        switch (picking.getType()) {
            case DELIVERY -> {
                StockItem item = itemOrThrow(picking.getSourceStockId(), productId);
                applyStockMovement.apply(picking.getSourceStockId(), productId, item.currentValue(),
                        StockMovementType.EXIT, -quantity, quantity, null, reference);
            }
            case RECEIPT -> {
                stockItemRepository.ensureItemExists(picking.getDestinationStockId(), productId);
                StockItem item = itemOrThrow(picking.getDestinationStockId(), productId);
                applyStockMovement.apply(picking.getDestinationStockId(), productId, item.currentValue(),
                        StockMovementType.ENTRY, quantity, quantity, null, reference);
            }
            case INTERNAL -> {
                StockItem from = itemOrThrow(picking.getSourceStockId(), productId);
                applyStockMovement.apply(picking.getSourceStockId(), productId, from.currentValue(),
                        StockMovementType.TRANSFER_OUT, -quantity, quantity, null, reference);
                stockItemRepository.ensureItemExists(picking.getDestinationStockId(), productId);
                StockItem to = itemOrThrow(picking.getDestinationStockId(), productId);
                applyStockMovement.apply(picking.getDestinationStockId(), productId, to.currentValue(),
                        StockMovementType.TRANSFER_IN, quantity, quantity, null, reference);
            }
            default -> throw new IllegalStateException("unsupported picking type " + picking.getType());
        }
    }

    private StockItem itemOrThrow(StockId stockId, ProductId productId) {
        return stockItemRepository.findByStockAndProduct(stockId, productId)
                .orElseThrow(() -> new NotFoundException("error.stockItem.notFoundInSource", productId));
    }

    public record Result(Picking picking, Picking backorder) {
    }
}
