package software.openlab.stock.application.usecase.reassignment;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import software.openlab.stock.application.usecase.movement.ApplyStockMovementUseCase;
import software.openlab.stock.application.usecase.stock.GetStockByIdUseCase;
import software.openlab.stock.domain.movement.StockMovementType;
import software.openlab.stock.domain.reassignment.Reassignment;
import software.openlab.stock.domain.reassignment.ReassignmentItem;
import software.openlab.stock.domain.reassignment.ReassignmentRepository;
import software.openlab.stock.domain.shared.ConflictException;
import software.openlab.stock.domain.shared.NotFoundException;
import software.openlab.stock.domain.shared.ProductId;
import software.openlab.stock.domain.stock.StockId;
import software.openlab.stock.domain.stockitem.StockItem;
import software.openlab.stock.domain.stockitem.StockItemRepository;

/**
 * POST /v1/reassignments — Requirement 5: a Reassignment now actually moves balance between
 * stocks (previously it was only recorded — {@code PanacheReassignmentRepository.save()} never
 * touched {@code Stock_Item.current_value}).
 */
@ApplicationScoped
public class CreateReassignmentUseCase {

    @Inject
    ReassignmentRepository reassignmentRepository;

    @Inject
    GetStockByIdUseCase getStockById;

    @Inject
    StockItemRepository stockItemRepository;

    @Inject
    ApplyStockMovementUseCase applyStockMovement;

    @Transactional
    public Reassignment execute(StockId fromStockId, StockId toStockId, List<ReassignmentItemRequest> items) {
        getStockById.execute(fromStockId);
        getStockById.execute(toStockId);

        List<ReassignmentItem> domainItems = items.stream()
                .map(item -> new ReassignmentItem(item.productId(), item.quantity()))
                .toList();

        // Requirement 5.1/5.2: validate ALL items have sufficient balance in from_stock_id
        // before applying ANY of them — tudo ou nada, no partial application.
        for (ReassignmentItem item : domainItems) {
            StockItem fromItem = stockItemRepository.findByStockAndProduct(fromStockId, item.getProductId())
                    .orElseThrow(() -> new NotFoundException("error.stockItem.notFoundInSource", item.getProductId()));
            if (fromItem.currentValue() < item.getQuantity()) {
                throw new ConflictException(
                        "error.reassignment.insufficientBalance", item.getProductId());
            }
        }

        Reassignment reassignment = Reassignment.create(fromStockId, toStockId, domainItems);
        reassignmentRepository.save(reassignment);

        String reference = reassignment.getReassignmentId().toString();
        for (ReassignmentItem item : domainItems) {
            ProductId productId = item.getProductId();
            int quantity = item.getQuantity();

            StockItem fromItem = stockItemRepository.findByStockAndProduct(fromStockId, productId).orElseThrow();
            applyStockMovement.apply(fromStockId, productId, fromItem.currentValue(), StockMovementType.TRANSFER_OUT,
                    -quantity, quantity, null, reference);

            stockItemRepository.ensureItemExists(toStockId, productId);
            StockItem toItem = stockItemRepository.findByStockAndProduct(toStockId, productId).orElseThrow();
            applyStockMovement.apply(toStockId, productId, toItem.currentValue(), StockMovementType.TRANSFER_IN,
                    quantity, quantity, null, reference);
        }

        return reassignment;
    }

    public record ReassignmentItemRequest(ProductId productId, int quantity) {
    }
}
