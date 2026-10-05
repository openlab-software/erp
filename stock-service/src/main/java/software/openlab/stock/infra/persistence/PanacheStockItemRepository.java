package software.openlab.stock.infra.persistence;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import software.openlab.stock.domain.shared.NotFoundException;
import software.openlab.stock.domain.shared.PageResult;
import software.openlab.stock.domain.shared.ProductId;
import software.openlab.stock.domain.stock.StockId;
import software.openlab.stock.domain.stockitem.StockItem;
import software.openlab.stock.domain.stockitem.StockItemRepository;

@ApplicationScoped
public class PanacheStockItemRepository implements StockItemRepository {

    @Inject
    CatalogProductLookup catalogProductLookup;

    @Override
    public PageResult<StockItem> findByStock(StockId stockId, ProductId productId, Boolean belowMin, Boolean aboveMax,
                                              int page, int pageSize) {
        StringBuilder hql = new StringBuilder("stock.publicId = ?1");
        List<Object> params = new ArrayList<>();
        params.add(stockId.toString());

        if (productId != null) {
            params.add(productId.toString());
            hql.append(" and catalogProductPublicId = ?").append(params.size());
        }
        if (Boolean.TRUE.equals(belowMin)) {
            hql.append(" and minValue is not null and currentValue < minValue");
        }
        if (Boolean.TRUE.equals(aboveMax)) {
            hql.append(" and maxValue is not null and currentValue > maxValue");
        }

        var query = StockItemEntity.find(hql.toString(), Sort.ascending("catalogProductPublicId"), params.toArray());
        long total = query.count();
        List<StockItemEntity> entities = query.page(Page.of(page - 1, pageSize)).list();
        List<StockItem> items = entities.stream().map(e -> toDomain(stockId, e)).toList();
        return new PageResult<>(items, page, pageSize, total);
    }

    @Override
    public Optional<StockItem> findByStockAndProduct(StockId stockId, ProductId productId) {
        return StockItemEntity.<StockItemEntity>find("stock.publicId = ?1 and catalogProductPublicId = ?2",
                stockId.toString(), productId.toString())
                .firstResultOptional()
                .map(e -> toDomain(stockId, e));
    }

    @Override
    public void updateCurrentValue(StockId stockId, ProductId productId, int newCurrentValue) {
        StockItemEntity entity = StockItemEntity.<StockItemEntity>find(
                "stock.publicId = ?1 and catalogProductPublicId = ?2", stockId.toString(), productId.toString())
                .firstResultOptional()
                .orElseThrow(() -> new NotFoundException(
                        "error.stockItem.notFound"));
        entity.currentValue = newCurrentValue;
        entity.modifiedAt = Instant.now();
    }

    @Override
    public void updateReservedValue(StockId stockId, ProductId productId, int newReservedValue) {
        StockItemEntity entity = StockItemEntity.<StockItemEntity>find(
                "stock.publicId = ?1 and catalogProductPublicId = ?2", stockId.toString(), productId.toString())
                .firstResultOptional()
                .orElseThrow(() -> new NotFoundException(
                        "error.stockItem.notFound"));
        entity.reservedValue = newReservedValue;
        entity.modifiedAt = Instant.now();
    }

    @Override
    public void ensureItemExists(StockId stockId, ProductId productId) {
        boolean exists = StockItemEntity.count("stock.publicId = ?1 and catalogProductPublicId = ?2",
                stockId.toString(), productId.toString()) > 0;
        if (exists) {
            return;
        }

        StockEntity stock = StockEntity.<StockEntity>find("publicId", stockId.toString())
                .firstResultOptional()
                .orElseThrow(() -> new NotFoundException("error.stock.notFound"));

        StockItemEntity entity = new StockItemEntity();
        entity.stock = stock;
        entity.catalogProductId = catalogProductLookup.findInternalId(productId).orElse(null);
        entity.catalogProductPublicId = productId.toString();
        entity.currentValue = 0;
        entity.reservedValue = 0;
        entity.active = true;
        entity.createdAt = Instant.now();
        entity.persist();
    }

    @Override
    public List<StockItem> findAllByProduct(ProductId productId) {
        List<StockItemEntity> entities = StockItemEntity.list("catalogProductPublicId = ?1 and active = true",
                productId.toString());
        return entities.stream().map(e -> toDomain(StockId.of(e.stock.publicId), e)).toList();
    }

    private static StockItem toDomain(StockId stockId, StockItemEntity e) {
        return new StockItem(stockId, ProductId.of(e.catalogProductPublicId), e.minValue, e.currentValue, e.maxValue,
                e.reservedValue, e.active);
    }
}
