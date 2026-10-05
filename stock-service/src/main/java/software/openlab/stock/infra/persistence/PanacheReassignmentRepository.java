package software.openlab.stock.infra.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.Optional;
import software.openlab.stock.domain.reassignment.Reassignment;
import software.openlab.stock.domain.reassignment.ReassignmentItem;
import software.openlab.stock.domain.reassignment.ReassignmentRepository;
import software.openlab.stock.domain.shared.BadRequestException;
import software.openlab.stock.domain.shared.NotFoundException;

@ApplicationScoped
public class PanacheReassignmentRepository implements ReassignmentRepository {

    @Inject
    CatalogProductLookup catalogProductLookup;

    @Override
    public void save(Reassignment reassignment) {
        StockEntity fromStock = StockEntity.<StockEntity>find("publicId", reassignment.getFromStockId().toString())
                .firstResultOptional()
                .orElseThrow(() -> new NotFoundException("error.stock.sourceNotFound"));
        StockEntity toStock = StockEntity.<StockEntity>find("publicId", reassignment.getToStockId().toString())
                .firstResultOptional()
                .orElseThrow(() -> new NotFoundException("error.stock.targetNotFound"));

        ReassignmentEntity entity = new ReassignmentEntity();
        entity.publicId = reassignment.getReassignmentId().toString();
        entity.fromStock = fromStock;
        entity.toStock = toStock;
        entity.createdAt = reassignment.getCreatedAt();
        entity.persist();

        for (ReassignmentItem item : reassignment.getItems()) {
            Optional<Long> catalogProductId = catalogProductLookup.findInternalId(item.getProductId());
            if (catalogProductId.isEmpty()) {
                throw new BadRequestException("error.product.notInCatalog", item.getProductId());
            }

            ReassignmentItemEntity itemEntity = new ReassignmentItemEntity();
            itemEntity.reassignment = entity;
            itemEntity.catalogProductId = catalogProductId.get();
            itemEntity.catalogProductPublicId = item.getProductId().toString();
            itemEntity.quantity = item.getQuantity();
            itemEntity.createdAt = Instant.now();
            itemEntity.persist();
        }
    }
}
