package software.openlab.catalog.domain.product;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.openlab.catalog.domain.brand.BrandId;
import software.openlab.catalog.domain.category.CategoryId;
import software.openlab.catalog.domain.supplier.SupplierId;
import software.openlab.catalog.domain.unitofmeasure.UnitOfMeasureId;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    private ProductId productId;
    private String description;
    private String shortDescription;
    private ProductType type;
    private UnitOfMeasureId unitOfMeasureId;
    private String unitOfMeasureCode;
    private ProductStatus status;
    private CategoryId categoryId;
    private String categoryDescription;
    private BrandId brandId;
    private String brandDescription;
    private SupplierId defaultSupplierId;
    private BigDecimal salePrice;
    private BigDecimal costPrice;
    private Instant createdAt;
    private Instant updatedAt;

    public static Product newProduct(String description, String shortDescription, ProductType type,
                                      UnitOfMeasureId unitOfMeasureId, CategoryId categoryId,
                                      BrandId brandId, SupplierId defaultSupplierId) {
        return new Product(ProductId.generate(), description, shortDescription, type, unitOfMeasureId, null,
                ProductStatus.DRAFT, categoryId, null, brandId, null, defaultSupplierId,
                BigDecimal.ZERO, BigDecimal.ZERO, Instant.now(), null);
    }
}
