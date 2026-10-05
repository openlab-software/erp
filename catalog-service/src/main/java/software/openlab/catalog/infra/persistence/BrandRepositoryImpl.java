package software.openlab.catalog.infra.persistence;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import software.openlab.catalog.domain.brand.Brand;
import software.openlab.catalog.domain.brand.BrandId;
import software.openlab.catalog.domain.brand.BrandRepository;
import software.openlab.catalog.domain.shared.PageResult;

@ApplicationScoped
public class BrandRepositoryImpl implements BrandRepository {

    @Override
    public Optional<Brand> findById(BrandId brandId) {
        return BrandEntity.<BrandEntity>find("publicId", brandId.toString())
                .firstResultOptional()
                .map(this::toDomain);
    }

    @Override
    public boolean existsByDescriptionIgnoreCase(String description, BrandId excludeBrandId) {
        if (excludeBrandId == null) {
            return BrandEntity.count("lower(description) = lower(?1)", description) > 0;
        }
        return BrandEntity.count("lower(description) = lower(?1) and publicId <> ?2", description, excludeBrandId.toString()) > 0;
    }

    @Override
    public PageResult<Brand> find(String q, int page, int pageSize) {
        Sort sort = Sort.by("createdAt", Sort.Direction.Descending);

        var query = (q == null || q.isBlank())
                ? BrandEntity.find("", sort)
                : BrandEntity.find("lower(description) like lower(?1)", sort, "%" + q + "%");

        long total = query.count();
        List<Brand> data = query.page(Page.of(page - 1, pageSize))
                .<BrandEntity>list()
                .stream()
                .map(this::toDomain)
                .toList();

        return new PageResult<>(data, page, pageSize, total);
    }

    @Override
    public void insert(Brand brand) {
        BrandEntity entity = new BrandEntity();
        entity.publicId = brand.getBrandId().toString();
        entity.description = brand.getDescription();
        entity.createdAt = brand.getCreatedAt();
        BrandEntity.persist(entity);
    }

    @Override
    public void update(Brand brand) {
        BrandEntity entity = BrandEntity.<BrandEntity>find("publicId", brand.getBrandId().toString())
                .firstResultOptional()
                .orElseThrow(() -> new IllegalStateException("brand not found: " + brand.getBrandId()));
        entity.description = brand.getDescription();
        entity.updatedAt = Instant.now();
    }

    @Override
    public void deleteById(BrandId brandId) {
        BrandEntity.delete("publicId", brandId.toString());
    }

    @Override
    public boolean existsById(BrandId brandId) {
        return BrandEntity.count("publicId", brandId.toString()) > 0;
    }

    @Override
    public boolean hasProducts(BrandId brandId) {
        return ProductEntity.count("brand.publicId = ?1", brandId.toString()) > 0;
    }

    private Brand toDomain(BrandEntity e) {
        return new Brand(BrandId.of(e.publicId), e.description, e.createdAt, e.updatedAt);
    }
}
