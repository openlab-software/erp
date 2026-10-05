package software.openlab.stock.infra.persistence;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import software.openlab.stock.domain.shared.NotFoundException;
import software.openlab.stock.domain.shared.PageResult;
import software.openlab.stock.domain.shared.ProductId;
import software.openlab.stock.domain.stock.StockId;
import software.openlab.stock.domain.stockcount.StockCount;
import software.openlab.stock.domain.stockcount.StockCountId;
import software.openlab.stock.domain.stockcount.StockCountRepository;

@ApplicationScoped
public class PanacheStockCountRepository implements StockCountRepository {

    @Override
    public void insert(StockCount count) {
        StockEntity stock = StockEntity.<StockEntity>find("publicId", count.getStockId().toString())
                .firstResultOptional()
                .orElseThrow(() -> new NotFoundException("error.stock.notFound"));

        StockCountEntity entity = new StockCountEntity();
        entity.publicId = count.getCountId().toString();
        entity.stock = stock;
        entity.catalogProductPublicId = count.getProductId().toString();
        entity.systemValue = count.getSystemValue();
        entity.countedValue = count.getCountedValue();
        entity.createdAt = count.getCreatedAt();
        entity.persist();
    }

    @Override
    public PageResult<StockCount> findByStock(StockId stockId, ProductId productId, int page, int pageSize) {
        StringBuilder hql = new StringBuilder("stock.publicId = ?1");
        List<Object> params = new ArrayList<>();
        params.add(stockId.toString());

        if (productId != null) {
            params.add(productId.toString());
            hql.append(" and catalogProductPublicId = ?").append(params.size());
        }

        var query = StockCountEntity.find(hql.toString(), Sort.descending("createdAt"), params.toArray());
        long total = query.count();
        List<StockCountEntity> entities = query.page(Page.of(page - 1, pageSize)).list();
        List<StockCount> counts = entities.stream().map(e -> toDomain(stockId, e)).toList();
        return new PageResult<>(counts, page, pageSize, total);
    }

    private static StockCount toDomain(StockId stockId, StockCountEntity e) {
        return StockCount.reconstruct(StockCountId.of(e.publicId), stockId, ProductId.of(e.catalogProductPublicId),
                e.systemValue, e.countedValue, e.createdAt);
    }
}
