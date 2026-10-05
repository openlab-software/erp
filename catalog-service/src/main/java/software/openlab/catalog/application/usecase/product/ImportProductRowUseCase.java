package software.openlab.catalog.application.usecase.product;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import software.openlab.catalog.domain.brand.BrandId;
import software.openlab.catalog.domain.category.CategoryId;
import software.openlab.catalog.domain.unitofmeasure.UnitOfMeasureId;

/**
 * Creates a single product from one CSV row (Requirement 14). Composes the
 * existing {@link CreateProductUseCase} and {@link ChangeProductPriceUseCase}
 * — both {@code @Transactional} with the default REQUIRED propagation — under
 * this class's own {@code @Transactional} boundary, so the whole row (create
 * + optional initial price) commits as exactly one database transaction, and
 * a failure anywhere in the row rolls back only that row (Requirement 14.2/14.5).
 * Must be called through the CDI-injected reference (never self-invoked) so
 * the transactional interceptor actually applies.
 */
@ApplicationScoped
public class ImportProductRowUseCase {

    @Inject
    CreateProductUseCase createProduct;

    @Inject
    ChangeProductPriceUseCase changeProductPrice;

    @Transactional
    public void execute(String description, String shortDescription, String type, UnitOfMeasureId unitOfMeasureId,
                         CategoryId categoryId, BrandId brandId, BigDecimal salePrice, BigDecimal costPrice) {
        var created = createProduct.execute(description, shortDescription, type, unitOfMeasureId, categoryId, brandId, null);

        if (salePrice != null || costPrice != null) {
            BigDecimal sp = salePrice != null ? salePrice : BigDecimal.ZERO;
            BigDecimal cp = costPrice != null ? costPrice : BigDecimal.ZERO;
            changeProductPrice.execute(created.getProductId(), sp, cp);
        }
    }
}
