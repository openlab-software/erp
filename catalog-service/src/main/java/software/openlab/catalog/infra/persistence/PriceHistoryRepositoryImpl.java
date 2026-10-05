package software.openlab.catalog.infra.persistence;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import software.openlab.catalog.domain.product.PriceHistory;
import software.openlab.catalog.domain.product.PriceHistoryId;
import software.openlab.catalog.domain.product.PriceHistoryRepository;
import software.openlab.catalog.domain.product.ProductId;
import software.openlab.catalog.domain.shared.NotFoundException;

@ApplicationScoped
public class PriceHistoryRepositoryImpl implements PriceHistoryRepository {

    @Override
    public void insert(PriceHistory priceHistory) {
        ProductEntity product = ProductEntity.<ProductEntity>find("publicId", priceHistory.productId().toString())
                .firstResultOptional()
                .orElseThrow(() -> new NotFoundException("error.product.notFound", priceHistory.productId()));

        PriceHistoryEntity entity = new PriceHistoryEntity();
        entity.publicId = priceHistory.priceHistoryId().toString();
        entity.product = product;
        entity.salePrice = priceHistory.salePrice();
        entity.costPrice = priceHistory.costPrice();
        entity.changedAt = priceHistory.changedAt();
        PriceHistoryEntity.persist(entity);
    }

    @Override
    public List<PriceHistory> findByProductId(ProductId productId) {
        return PriceHistoryEntity.<PriceHistoryEntity>find("product.publicId", Sort.by("changedAt", Sort.Direction.Descending), productId.toString())
                .list()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private PriceHistory toDomain(PriceHistoryEntity e) {
        return new PriceHistory(
                PriceHistoryId.of(e.publicId),
                ProductId.of(e.product.publicId),
                e.salePrice,
                e.costPrice,
                e.changedAt
        );
    }
}
