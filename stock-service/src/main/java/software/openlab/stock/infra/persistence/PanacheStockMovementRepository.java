package software.openlab.stock.infra.persistence;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import software.openlab.stock.domain.movement.StockMovement;
import software.openlab.stock.domain.movement.StockMovementId;
import software.openlab.stock.domain.movement.StockMovementRepository;
import software.openlab.stock.domain.movement.StockMovementType;
import software.openlab.stock.domain.movementreason.MovementReasonId;
import software.openlab.stock.domain.shared.BadRequestException;
import software.openlab.stock.domain.shared.NotFoundException;
import software.openlab.stock.domain.shared.PageResult;
import software.openlab.stock.domain.shared.ProductId;
import software.openlab.stock.domain.stock.StockId;

@ApplicationScoped
public class PanacheStockMovementRepository implements StockMovementRepository {

    @Override
    public void insert(StockMovement movement) {
        StockEntity stock = StockEntity.<StockEntity>find("publicId", movement.getStockId().toString())
                .firstResultOptional()
                .orElseThrow(() -> new NotFoundException("error.stock.notFound"));

        StockMovementEntity entity = new StockMovementEntity();
        entity.publicId = movement.getMovementId().toString();
        entity.stock = stock;
        entity.catalogProductPublicId = movement.getProductId().toString();
        entity.type = movement.getType().name();
        entity.quantity = movement.getQuantity();
        entity.resultingBalance = movement.getResultingBalance();
        entity.reason = movement.getReasonId() == null ? null : findReasonEntityOrThrow(movement.getReasonId());
        entity.reference = movement.getReference();
        entity.createdAt = movement.getCreatedAt();
        entity.persist();
    }

    @Override
    public PageResult<StockMovement> findByStock(StockId stockId, ProductId productId, StockMovementType type,
                                                  Instant from, Instant to, int page, int pageSize) {
        StringBuilder hql = new StringBuilder("stock.publicId = ?1");
        List<Object> params = new ArrayList<>();
        params.add(stockId.toString());

        if (productId != null) {
            params.add(productId.toString());
            hql.append(" and catalogProductPublicId = ?").append(params.size());
        }
        if (type != null) {
            params.add(type.name());
            hql.append(" and type = ?").append(params.size());
        }
        if (from != null) {
            params.add(from);
            hql.append(" and createdAt >= ?").append(params.size());
        }
        if (to != null) {
            params.add(to);
            hql.append(" and createdAt <= ?").append(params.size());
        }

        var query = StockMovementEntity.find(hql.toString(), Sort.descending("createdAt"), params.toArray());
        long total = query.count();
        List<StockMovementEntity> entities = query.page(Page.of(page - 1, pageSize)).list();
        List<StockMovement> movements = entities.stream().map(e -> toDomain(stockId, e)).toList();
        return new PageResult<>(movements, page, pageSize, total);
    }

    @Override
    public boolean existsByReasonId(MovementReasonId reasonId) {
        return StockMovementEntity.count("reason.publicId = ?1", reasonId.toString()) > 0;
    }

    private MovementReasonEntity findReasonEntityOrThrow(MovementReasonId reasonId) {
        return MovementReasonEntity.<MovementReasonEntity>find("publicId", reasonId.toString())
                .firstResultOptional()
                .orElseThrow(() -> new BadRequestException(
                        "error.movementReason.refInvalid"));
    }

    private static StockMovement toDomain(StockId stockId, StockMovementEntity e) {
        MovementReasonId reasonId = e.reason == null ? null : MovementReasonId.of(e.reason.publicId);
        return StockMovement.reconstruct(
                StockMovementId.of(e.publicId),
                stockId,
                ProductId.of(e.catalogProductPublicId),
                StockMovementType.valueOf(e.type),
                e.quantity,
                e.resultingBalance,
                reasonId,
                e.reference,
                e.createdAt);
    }
}
