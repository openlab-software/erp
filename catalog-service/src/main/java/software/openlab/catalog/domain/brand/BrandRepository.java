package software.openlab.catalog.domain.brand;

import java.util.Optional;
import software.openlab.catalog.domain.shared.PageResult;

public interface BrandRepository {

    Optional<Brand> findById(BrandId brandId);

    boolean existsByDescriptionIgnoreCase(String description, BrandId excludeBrandId);

    PageResult<Brand> find(String q, int page, int pageSize);

    void insert(Brand brand);

    void update(Brand brand);

    void deleteById(BrandId brandId);

    boolean existsById(BrandId brandId);

    boolean hasProducts(BrandId brandId);
}
