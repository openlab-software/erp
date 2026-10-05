package software.openlab.catalog.application.usecase.product;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.openlab.catalog.domain.brand.BrandId;
import software.openlab.catalog.domain.category.CategoryId;
import software.openlab.catalog.domain.product.Product;
import software.openlab.catalog.domain.product.ProductFilter;
import software.openlab.catalog.domain.product.ProductRepository;
import software.openlab.catalog.domain.product.ProductStatus;
import software.openlab.catalog.domain.product.ProductType;
import software.openlab.catalog.domain.shared.PageResult;

@ApplicationScoped
public class ListProductsUseCase {

    @Inject
    ProductRepository repository;

    public PageResult<Product> execute(String q, CategoryId categoryId, String status, String type,
                                        BrandId brandId, Integer page, Integer pageSize) {
        int normalizedPage = PageResult.normalizePage(page);
        int normalizedPageSize = PageResult.normalizePageSize(pageSize);
        ProductStatus parsedStatus = (status == null || status.isBlank()) ? null : ProductStatus.parse(status);
        ProductType parsedType = (type == null || type.isBlank()) ? null : ProductType.parse(type);

        ProductFilter filter = new ProductFilter(q, categoryId, parsedStatus, parsedType, brandId, normalizedPage, normalizedPageSize);
        return repository.find(filter);
    }
}
