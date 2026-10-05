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
import software.openlab.catalog.domain.product.ProductRepository;
import software.openlab.catalog.domain.product.ProductType;
import software.openlab.catalog.domain.shared.BadRequestException;
import software.openlab.catalog.domain.shared.DomainEvent;
import software.openlab.catalog.domain.shared.Fields;
import software.openlab.catalog.domain.supplier.SupplierId;
import software.openlab.catalog.domain.supplier.SupplierRepository;
import software.openlab.catalog.domain.unitofmeasure.UnitOfMeasureId;
import software.openlab.catalog.domain.unitofmeasure.UnitOfMeasureRepository;

@ApplicationScoped
public class CreateProductUseCase {

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
    public Product execute(String description, String shortDescription, String typeRaw,
                            UnitOfMeasureId unitOfMeasureId, CategoryId categoryId,
                            BrandId brandId, SupplierId defaultSupplierId) {
        String d = Fields.requireNonBlank(description, "description");
        String sd = Fields.requireNonBlank(shortDescription, "short_description");
        ProductType type = ProductType.parse(typeRaw);

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

        Product product = Product.newProduct(d, sd, type, unitOfMeasureId, categoryId, brandId, defaultSupplierId);
        repository.insert(product);

        events.fire(new DomainEvent(ProductEvents.CREATED,
                new ProductEvents.ProductCreatedPayload(
                        product.getProductId().toString(),
                        product.getDescription(),
                        product.getType().name(),
                        product.getUnitOfMeasureId().toString(),
                        product.getCategoryId().toString(),
                        product.getBrandId() != null ? product.getBrandId().toString() : null,
                        product.getDefaultSupplierId() != null ? product.getDefaultSupplierId().toString() : null)));

        return getProductById.execute(product.getProductId());
    }
}
