package software.openlab.catalog.application.usecase.product;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import software.openlab.catalog.domain.product.ProductBarcode;
import software.openlab.catalog.domain.product.ProductBarcodeId;
import software.openlab.catalog.domain.product.ProductBarcodeRepository;
import software.openlab.catalog.domain.product.ProductEvents;
import software.openlab.catalog.domain.product.ProductEvents.BarcodeItem;
import software.openlab.catalog.domain.product.ProductId;
import software.openlab.catalog.domain.shared.DomainEvent;
import software.openlab.catalog.domain.shared.NotFoundException;

@ApplicationScoped
public class DeleteProductBarcodeUseCase {

    @Inject
    ProductBarcodeRepository repository;

    @Inject
    Event<DomainEvent> events;

    @Inject
    GetProductByIdUseCase getProductById;

    @Transactional
    public void execute(ProductId productId, ProductBarcodeId barcodeId) {
        getProductById.execute(productId);

        ProductBarcode barcode = repository.findById(barcodeId)
                .filter(b -> b.getProductId().equals(productId))
                .orElseThrow(() -> new NotFoundException("error.barcode.notFound", barcodeId));

        repository.deleteById(barcode.getBarcodeId());

        List<BarcodeItem> current = repository.findByProductId(productId).stream().map(BarcodeItem::from).toList();
        events.fire(new DomainEvent(ProductEvents.UPDATED,
                new ProductEvents.ProductBarcodesUpdatedPayload(productId.toString(), current)));
    }
}
