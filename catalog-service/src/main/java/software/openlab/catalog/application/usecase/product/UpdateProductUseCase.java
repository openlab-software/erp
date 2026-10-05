package software.openlab.catalog.application.usecase.product;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.catalog.domain.brand.BrandId;
import software.openlab.catalog.domain.brand.BrandRepository;
import software.openlab.catalog.domain.category.CategoryId;
import software.openlab.catalog.domain.category.CategoryRepository;
import software.openlab.catalog.domain.product.Product;
import software.openlab.catalog.domain.product.ProductEvents;
import software.openlab.catalog.domain.product.ProductId;
import software.openlab.catalog.domain.product.ProductRepository;
import software.openlab.catalog.domain.shared.BadRequestException;
import software.openlab.catalog.domain.shared.DomainEvent;
import software.openlab.catalog.domain.shared.Fields;
import software.openlab.catalog.domain.supplier.SupplierId;
import software.openlab.catalog.domain.supplier.SupplierRepository;
import software.openlab.catalog.domain.unitofmeasure.UnitOfMeasureId;
import software.openlab.catalog.domain.unitofmeasure.UnitOfMeasureRepository;

/**
 * {@code type} is intentionally not a parameter here: it is immutable after
 * creation (Requirement 6.3) and this use case never touches it, even if the
 * caller's payload carried one — the existing value is simply preserved.
 */
@ApplicationScoped
public class UpdateProductUseCase {

    @Inject
    ProductRepository repository;

    @Inject
    CategoryRepository categoryRepository;

    @Inject
    UnitOfMeasureRepository unitOfMeasureRepository;

    @Inject
    BrandRepository brandRepository;

    @Inject
    SupplierRepository supplierRepository;

    @Inject
    Event<DomainEvent> events;

    @Inject
    GetProductByIdUseCase getProductById;

    @Transactional
    public Product execute(ProductId productId, String description, String shortDescription,
                            UnitOfMeasureId unitOfMeasureId, CategoryId categoryId,
                            BrandId brandId, SupplierId defaultSupplierId) {
        String d = Fields.requireNonBlank(description, "description");
        String sd = Fields.requireNonBlank(shortDescription, "short_description");

        Product product = getProductById.execute(productId);

        if (!categoryRepository.existsById(categoryId)) {
            throw new BadRequestException("error.ref.category", "category_id");
        }
        if (!unitOfMeasureRepository.existsById(unitOfMeasureId)) {
            throw new BadRequestException("error.ref.unitOfMeasure", "unit_of_measure_id");
        }
        if (brandId != null && !brandRepository.existsById(brandId)) {
            throw new BadRequestException("error.ref.brand", "brand_id");
        }
        if (defaultSupplierId != null && !supplierRepository.existsById(defaultSupplierId)) {
            throw new BadRequestException("error.ref.supplier", "default_supplier_id");
        }

        product.setDescription(d);
        product.setShortDescription(sd);
        product.setUnitOfMeasureId(unitOfMeasureId);
        product.setCategoryId(categoryId);
        product.setBrandId(brandId);
        product.setDefaultSupplierId(defaultSupplierId);
        // status is preserved — only ChangeProductStatusUseCase changes it (Requirement 5.6)
        // type is preserved — immutable after creation (Requirement 6.3)

        repository.update(product);

        Product updated = getProductById.execute(productId);
        events.fire(new DomainEvent(ProductEvents.UPDATED, ProductEvents.updatedPayloadOf(updated)));

        return updated;
    }
}
