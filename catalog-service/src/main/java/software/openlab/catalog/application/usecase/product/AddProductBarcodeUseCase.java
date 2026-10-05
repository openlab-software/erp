package software.openlab.catalog.application.usecase.product;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import software.openlab.catalog.domain.product.BarcodeType;
import software.openlab.catalog.domain.product.ProductBarcode;
import software.openlab.catalog.domain.product.ProductBarcodeRepository;
import software.openlab.catalog.domain.product.ProductEvents;
import software.openlab.catalog.domain.product.ProductEvents.BarcodeItem;
import software.openlab.catalog.domain.product.ProductId;
import software.openlab.catalog.domain.shared.ConflictException;
import software.openlab.catalog.domain.shared.DomainEvent;
import software.openlab.catalog.domain.shared.Fields;

@ApplicationScoped
public class AddProductBarcodeUseCase {

    @Inject
    ProductBarcodeRepository repository;

    @Inject
    Event<DomainEvent> events;

    @Inject
    GetProductByIdUseCase getProductById;

    @Transactional
    public ProductBarcode execute(ProductId productId, String code, String typeRaw) {
        getProductById.execute(productId);

        String trimmedCode = Fields.requireNonBlank(code, "code");
        BarcodeType type = BarcodeType.parse(typeRaw);

        if (repository.existsByCode(trimmedCode)) {
            throw new ConflictException("error.barcode.duplicate");
        }

        ProductBarcode barcode = ProductBarcode.newProductBarcode(productId, trimmedCode, type);
        repository.insert(barcode);

        List<BarcodeItem> current = repository.findByProductId(productId).stream().map(BarcodeItem::from).toList();
        events.fire(new DomainEvent(ProductEvents.UPDATED,
                new ProductEvents.ProductBarcodesUpdatedPayload(productId.toString(), current)));

        return barcode;
    }
}
