package software.openlab.catalog.domain.product;

import software.openlab.catalog.domain.brand.BrandId;
import software.openlab.catalog.domain.category.CategoryId;

public record ProductFilter(String q, CategoryId categoryId, ProductStatus status, ProductType type,
                             BrandId brandId, int page, int pageSize) {
}
