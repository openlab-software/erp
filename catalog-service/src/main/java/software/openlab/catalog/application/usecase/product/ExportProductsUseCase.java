package software.openlab.catalog.application.usecase.product;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import software.openlab.catalog.domain.brand.BrandId;
import software.openlab.catalog.domain.category.CategoryId;
import software.openlab.catalog.domain.product.Product;
import software.openlab.catalog.domain.product.ProductFilter;
import software.openlab.catalog.domain.product.ProductRepository;
import software.openlab.catalog.domain.product.ProductStatus;
import software.openlab.catalog.domain.product.ProductType;

@ApplicationScoped
public class ExportProductsUseCase {

    @Inject
    ProductRepository repository;

    public List<Product> execute(String q, CategoryId categoryId, String status, String type, BrandId brandId) {
        ProductStatus parsedStatus = (status == null || status.isBlank()) ? null : ProductStatus.parse(status);
        ProductType parsedType = (type == null || type.isBlank()) ? null : ProductType.parse(type);

        // page/pageSize are irrelevant here — findAll ignores them (Requirement 15.3).
        ProductFilter filter = new ProductFilter(q, categoryId, parsedStatus, parsedType, brandId, 1, 1);
        return repository.findAll(filter);
    }
}
