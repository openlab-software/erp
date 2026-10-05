package software.openlab.stock.infra.persistence;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.jboss.logging.Logger;
import software.openlab.stock.domain.shared.NotFoundException;
import software.openlab.stock.domain.shared.PageResult;
import software.openlab.stock.domain.shared.ProductId;
import software.openlab.stock.domain.stock.Stock;
import software.openlab.stock.domain.stock.StockId;
import software.openlab.stock.domain.stock.StockRepository;

@ApplicationScoped
public class PanacheStockRepository implements StockRepository {

    private static final Logger LOG = Logger.getLogger(PanacheStockRepository.class);

    @Inject
    CatalogProductLookup catalogProductLookup;

    @Override
    public PageResult<Stock> findAll(String q, int page, int pageSize) {
        var query = (q != null && !q.isBlank())
                ? StockEntity.find("lower(description) like ?1", Sort.descending("createdAt"),
                    "%" + q.toLowerCase() + "%")
                : StockEntity.findAll(Sort.descending("createdAt"));

        long total = query.count();
        List<StockEntity> entities = query.page(Page.of(page - 1, pageSize)).list();
        List<Stock> stocks = entities.stream().map(PanacheStockRepository::toDomain).toList();
        return new PageResult<>(stocks, page, pageSize, total);
    }

    @Override
    public Optional<Stock> findById(StockId stockId) {
        return StockEntity.<StockEntity>find("publicId", stockId.toString())
                .firstResultOptional()
                .map(PanacheStockRepository::toDomain);
    }

    @Override
    public boolean existsByDescriptionIgnoreCase(String description) {
        return StockEntity.count("lower(description) = ?1", description.toLowerCase()) > 0;
    }

    @Override
    public boolean existsByDescriptionIgnoreCaseExcluding(String description, StockId excludeStockId) {
        return StockEntity.count("lower(description) = ?1 and publicId != ?2",
                description.toLowerCase(), excludeStockId.toString()) > 0;
    }

    @Override
    public void insert(Stock stock) {
        StockEntity entity = new StockEntity();
        entity.publicId = stock.getStockId().toString();
        entity.description = stock.getDescription();
        entity.createdAt = stock.getCreatedAt();
        entity.modifiedAt = stock.getModifiedAt();
        entity.persist();
    }

    @Override
    public void update(Stock stock) {
        StockEntity entity = findEntityOrThrow(stock.getStockId());
        entity.description = stock.getDescription();
        entity.modifiedAt = stock.getModifiedAt();
    }

    @Override
    public void deleteCascade(StockId stockId) {
        StockEntity entity = findEntityOrThrow(stockId);
        StockItemEntity.delete("stock", entity);
        entity.delete();
    }

    @Override
    public boolean hasActiveItems(StockId stockId) {
        return StockItemEntity.count("stock.publicId = ?1 and currentValue > 0", stockId.toString()) > 0;
    }

    @Override
    public boolean hasReservedItems(StockId stockId) {
        return StockItemEntity.count("stock.publicId = ?1 and reservedValue > 0", stockId.toString()) > 0;
    }

    @Override
    public boolean isReferencedInAnyReassignment(StockId stockId) {
        return ReassignmentEntity.count("fromStock.publicId = ?1 or toStock.publicId = ?1", stockId.toString()) > 0;
    }

    @Override
    public void createEmptyItemsForProductInAllStocks(ProductId productId) {
        Optional<Long> catalogProductId = catalogProductLookup.findInternalId(productId);
        if (catalogProductId.isEmpty()) {
            LOG.warnf("product.created for unknown catalog product public_id=%s — skipping item creation", productId);
            return;
        }

        String productPublicId = productId.toString();
        List<StockEntity> allStocks = StockEntity.listAll();
        for (StockEntity stock : allStocks) {
            boolean alreadyExists = StockItemEntity.count("stock = ?1 and catalogProductPublicId = ?2",
                    stock, productPublicId) > 0;
            if (alreadyExists) {
                continue;
            }
            StockItemEntity item = new StockItemEntity();
            item.stock = stock;
            item.catalogProductId = catalogProductId.get();
            item.catalogProductPublicId = productPublicId;
            item.currentValue = 0;
            item.active = true;
            item.createdAt = Instant.now();
            item.persist();
        }
    }

    @Override
    public void deactivateItemsForProduct(ProductId productId) {
        StockItemEntity.update("active = false, modifiedAt = ?1 where catalogProductPublicId = ?2",
                Instant.now(), productId.toString());
    }

    @Override
    public void deleteItemsForProduct(ProductId productId) {
        StockItemEntity.delete("catalogProductPublicId", productId.toString());
    }

    private StockEntity findEntityOrThrow(StockId stockId) {
        return StockEntity.<StockEntity>find("publicId", stockId.toString())
                .firstResultOptional()
                .orElseThrow(() -> new NotFoundException("error.stock.notFound"));
    }

    private static Stock toDomain(StockEntity e) {
        return Stock.reconstruct(StockId.of(e.publicId), e.description, e.createdAt, e.modifiedAt);
    }
}
