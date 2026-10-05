package software.openlab.catalog.application.usecase.brand;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.openlab.catalog.domain.brand.Brand;
import software.openlab.catalog.domain.brand.BrandRepository;
import software.openlab.catalog.domain.shared.PageResult;

@ApplicationScoped
public class ListBrandsUseCase {

    @Inject
    BrandRepository repository;

    public PageResult<Brand> execute(String q, Integer page, Integer pageSize) {
        int normalizedPage = PageResult.normalizePage(page);
        int normalizedPageSize = PageResult.normalizePageSize(pageSize);
        return repository.find(q, normalizedPage, normalizedPageSize);
    }
}
