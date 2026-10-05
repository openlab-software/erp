package software.openlab.catalog.infra.persistence;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import software.openlab.catalog.domain.brand.BrandId;
import software.openlab.catalog.domain.category.CategoryId;
import software.openlab.catalog.domain.product.Product;
import software.openlab.catalog.domain.product.ProductFilter;
import software.openlab.catalog.domain.product.ProductId;
import software.openlab.catalog.domain.product.ProductRepository;
import software.openlab.catalog.domain.product.ProductStatus;
import software.openlab.catalog.domain.product.ProductType;
import software.openlab.catalog.domain.shared.NotFoundException;
import software.openlab.catalog.domain.shared.PageResult;
import software.openlab.catalog.domain.supplier.SupplierId;
import software.openlab.catalog.domain.unitofmeasure.UnitOfMeasureId;

@ApplicationScoped
public class ProductRepositoryImpl implements ProductRepository {

    @Override
    public Optional<Product> findById(ProductId productId) {
        return ProductEntity.<ProductEntity>find("publicId", productId.toString())
                .firstResultOptional()
                .map(this::toDomain);
    }

    @Override
    public PageResult<Product> find(ProductFilter filter) {
        FilterClauses clauses = buildClauses(filter);
        Sort sort = Sort.by("createdAt", Sort.Direction.Descending);

        var panacheQuery = clauses.query().isEmpty()
                ? ProductEntity.find("", sort)
                : ProductEntity.find(clauses.query(), sort, clauses.params());

        long total = panacheQuery.count();
        List<Product> data = panacheQuery.page(Page.of(filter.page() - 1, filter.pageSize()))
                .<ProductEntity>list()
                .stream()
                .map(this::toDomain)
                .toList();

        return new PageResult<>(data, filter.page(), filter.pageSize(), total);
    }

    @Override
    public List<Product> findAll(ProductFilter filter) {
        FilterClauses clauses = buildClauses(filter);
        Sort sort = Sort.by("createdAt", Sort.Direction.Descending);

        var panacheQuery = clauses.query().isEmpty()
                ? ProductEntity.find("", sort)
                : ProductEntity.find(clauses.query(), sort, clauses.params());

        return panacheQuery.<ProductEntity>list().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Product> autocomplete(String q, int limit) {
        Map<String, Object> params = new HashMap<>();
        params.put("contains", "%" + q + "%");
        params.put("prefix", q + "%");
        params.put("inactive", ProductStatus.INACTIVE.name());

        String query = "lower(description) like lower(:contains) and status <> :inactive "
                + "order by case when lower(description) like lower(:prefix) then 0 else 1 end, description";

        return ProductEntity.find(query, params)
                .page(Page.ofSize(limit))
                .<ProductEntity>list()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private FilterClauses buildClauses(ProductFilter filter) {
        List<String> clauses = new ArrayList<>();
        Map<String, Object> paramMap = new HashMap<>();

        if (filter.q() != null && !filter.q().isBlank()) {
            clauses.add("(lower(description) like lower(:q) or lower(shortDescription) like lower(:q))");
            paramMap.put("q", "%" + filter.q() + "%");
        }
        if (filter.categoryId() != null) {
            clauses.add("category.publicId = :categoryId");
            paramMap.put("categoryId", filter.categoryId().toString());
        }
        if (filter.status() != null) {
            clauses.add("status = :status");
            paramMap.put("status", filter.status().name());
        }
        if (filter.type() != null) {
            clauses.add("type = :type");
            paramMap.put("type", filter.type().name());
        }
        if (filter.brandId() != null) {
            clauses.add("brand.publicId = :brandId");
            paramMap.put("brandId", filter.brandId().toString());
        }

        return new FilterClauses(String.join(" and ", clauses), paramMap);
    }

    private record FilterClauses(String query, Map<String, Object> params) {
    }

    @Override
    public void insert(Product product) {
        CategoryEntity category = CategoryEntity.<CategoryEntity>find("publicId", product.getCategoryId().toString())
                .firstResultOptional()
                .orElseThrow(() -> new NotFoundException("error.category.notFound", product.getCategoryId()));

        ProductEntity entity = new ProductEntity();
        entity.publicId = product.getProductId().toString();
        entity.description = product.getDescription();
        entity.shortDescription = product.getShortDescription();
        entity.type = product.getType().name();
        entity.unitOfMeasure = resolveUnitOfMeasure(product.getUnitOfMeasureId());
        entity.status = product.getStatus().name();
        entity.category = category;
        entity.brand = resolveBrand(product.getBrandId());
        entity.defaultSupplier = resolveSupplier(product.getDefaultSupplierId());
        entity.salePrice = product.getSalePrice();
        entity.costPrice = product.getCostPrice();
        entity.createdAt = product.getCreatedAt();
        ProductEntity.persist(entity);
    }

    @Override
    public void update(Product product) {
        ProductEntity entity = ProductEntity.<ProductEntity>find("publicId", product.getProductId().toString())
                .firstResultOptional()
                .orElseThrow(() -> new IllegalStateException("product not found: " + product.getProductId()));

        entity.description = product.getDescription();
        entity.shortDescription = product.getShortDescription();
        // type is immutable after creation (Requirement 6.3) — intentionally never written here.
        entity.status = product.getStatus().name();
        entity.salePrice = product.getSalePrice();
        entity.costPrice = product.getCostPrice();
        entity.updatedAt = Instant.now();

        if (product.getCategoryId() != null && !product.getCategoryId().toString().equals(entity.category.publicId)) {
            CategoryEntity category = CategoryEntity.<CategoryEntity>find("publicId", product.getCategoryId().toString())
                    .firstResultOptional()
                    .orElseThrow(() -> new NotFoundException("error.category.notFound", product.getCategoryId()));
            entity.category = category;
        }

        if (product.getUnitOfMeasureId() != null && !product.getUnitOfMeasureId().toString().equals(entity.unitOfMeasure.publicId)) {
            entity.unitOfMeasure = resolveUnitOfMeasure(product.getUnitOfMeasureId());
        }

        entity.brand = resolveBrand(product.getBrandId());
        entity.defaultSupplier = resolveSupplier(product.getDefaultSupplierId());
    }

    @Override
    public void deleteById(ProductId productId) {
        ProductEntity.delete("publicId", productId.toString());
    }

    @Override
    public boolean existsById(ProductId productId) {
        return ProductEntity.count("publicId", productId.toString()) > 0;
    }

    private UnitOfMeasureEntity resolveUnitOfMeasure(UnitOfMeasureId unitOfMeasureId) {
        return UnitOfMeasureEntity.<UnitOfMeasureEntity>find("publicId", unitOfMeasureId.toString())
                .firstResultOptional()
                .orElseThrow(() -> new NotFoundException("error.unitOfMeasure.notFound", unitOfMeasureId));
    }

    private BrandEntity resolveBrand(BrandId brandId) {
        if (brandId == null) {
            return null;
        }
        return BrandEntity.<BrandEntity>find("publicId", brandId.toString())
                .firstResultOptional()
                .orElseThrow(() -> new NotFoundException("error.brand.notFound", brandId));
    }

    private SupplierEntity resolveSupplier(SupplierId supplierId) {
        if (supplierId == null) {
            return null;
        }
        return SupplierEntity.<SupplierEntity>find("publicId", supplierId.toString())
                .firstResultOptional()
                .orElseThrow(() -> new NotFoundException("error.supplier.notFound", supplierId));
    }

    private Product toDomain(ProductEntity e) {
        return new Product(
                ProductId.of(e.publicId),
                e.description,
                e.shortDescription,
                ProductType.valueOf(e.type),
                UnitOfMeasureId.of(e.unitOfMeasure.publicId),
                e.unitOfMeasure.code,
                ProductStatus.valueOf(e.status),
                CategoryId.of(e.category.publicId),
                e.category.description,
                e.brand != null ? BrandId.of(e.brand.publicId) : null,
                e.brand != null ? e.brand.description : null,
                e.defaultSupplier != null ? SupplierId.of(e.defaultSupplier.publicId) : null,
                e.salePrice,
                e.costPrice,
                e.createdAt,
                e.updatedAt
        );
    }
}
