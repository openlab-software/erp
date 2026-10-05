package software.openlab.catalog.infra.persistence;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import software.openlab.catalog.domain.shared.PageResult;
import software.openlab.catalog.domain.unitofmeasure.UnitOfMeasure;
import software.openlab.catalog.domain.unitofmeasure.UnitOfMeasureId;
import software.openlab.catalog.domain.unitofmeasure.UnitOfMeasureRepository;

@ApplicationScoped
public class UnitOfMeasureRepositoryImpl implements UnitOfMeasureRepository {

    @Override
    public Optional<UnitOfMeasure> findById(UnitOfMeasureId unitOfMeasureId) {
        return UnitOfMeasureEntity.<UnitOfMeasureEntity>find("publicId", unitOfMeasureId.toString())
                .firstResultOptional()
                .map(this::toDomain);
    }

    @Override
    public Optional<UnitOfMeasure> findByCodeIgnoreCase(String code) {
        return UnitOfMeasureEntity.<UnitOfMeasureEntity>find("lower(code) = lower(?1)", code)
                .firstResultOptional()
                .map(this::toDomain);
    }

    @Override
    public boolean existsByCodeIgnoreCase(String code, UnitOfMeasureId excludeUnitOfMeasureId) {
        if (excludeUnitOfMeasureId == null) {
            return UnitOfMeasureEntity.count("lower(code) = lower(?1)", code) > 0;
        }
        return UnitOfMeasureEntity.count("lower(code) = lower(?1) and publicId <> ?2", code, excludeUnitOfMeasureId.toString()) > 0;
    }

    @Override
    public PageResult<UnitOfMeasure> find(String q, int page, int pageSize) {
        Sort sort = Sort.by("createdAt", Sort.Direction.Descending);

        var query = (q == null || q.isBlank())
                ? UnitOfMeasureEntity.find("", sort)
                : UnitOfMeasureEntity.find("lower(description) like lower(?1)", sort, "%" + q + "%");

        long total = query.count();
        List<UnitOfMeasure> data = query.page(Page.of(page - 1, pageSize))
                .<UnitOfMeasureEntity>list()
                .stream()
                .map(this::toDomain)
                .toList();

        return new PageResult<>(data, page, pageSize, total);
    }

    @Override
    public void insert(UnitOfMeasure unitOfMeasure) {
        UnitOfMeasureEntity entity = new UnitOfMeasureEntity();
        entity.publicId = unitOfMeasure.getUnitOfMeasureId().toString();
        entity.code = unitOfMeasure.getCode();
        entity.description = unitOfMeasure.getDescription();
        entity.createdAt = unitOfMeasure.getCreatedAt();
        UnitOfMeasureEntity.persist(entity);
    }

    @Override
    public void update(UnitOfMeasure unitOfMeasure) {
        UnitOfMeasureEntity entity = UnitOfMeasureEntity.<UnitOfMeasureEntity>find("publicId", unitOfMeasure.getUnitOfMeasureId().toString())
                .firstResultOptional()
                .orElseThrow(() -> new IllegalStateException("unit of measure not found: " + unitOfMeasure.getUnitOfMeasureId()));
        entity.code = unitOfMeasure.getCode();
        entity.description = unitOfMeasure.getDescription();
        entity.updatedAt = Instant.now();
    }

    @Override
    public void deleteById(UnitOfMeasureId unitOfMeasureId) {
        UnitOfMeasureEntity.delete("publicId", unitOfMeasureId.toString());
    }

    @Override
    public boolean existsById(UnitOfMeasureId unitOfMeasureId) {
        return UnitOfMeasureEntity.count("publicId", unitOfMeasureId.toString()) > 0;
    }

    @Override
    public boolean hasProducts(UnitOfMeasureId unitOfMeasureId) {
        return ProductEntity.count("unitOfMeasure.publicId = ?1", unitOfMeasureId.toString()) > 0;
    }

    private UnitOfMeasure toDomain(UnitOfMeasureEntity e) {
        return new UnitOfMeasure(UnitOfMeasureId.of(e.publicId), e.code, e.description, e.createdAt, e.updatedAt);
    }
}
