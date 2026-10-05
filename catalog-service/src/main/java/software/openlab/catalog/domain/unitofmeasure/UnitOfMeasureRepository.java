package software.openlab.catalog.domain.unitofmeasure;

import java.util.Optional;
import software.openlab.catalog.domain.shared.PageResult;

public interface UnitOfMeasureRepository {

    Optional<UnitOfMeasure> findById(UnitOfMeasureId unitOfMeasureId);

    Optional<UnitOfMeasure> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code, UnitOfMeasureId excludeUnitOfMeasureId);

    PageResult<UnitOfMeasure> find(String q, int page, int pageSize);

    void insert(UnitOfMeasure unitOfMeasure);

    void update(UnitOfMeasure unitOfMeasure);

    void deleteById(UnitOfMeasureId unitOfMeasureId);

    boolean existsById(UnitOfMeasureId unitOfMeasureId);

    boolean hasProducts(UnitOfMeasureId unitOfMeasureId);
}
