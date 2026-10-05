package software.openlab.catalog.application.usecase.product;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import software.openlab.catalog.domain.product.ProductEvents;
import software.openlab.catalog.domain.product.ProductEvents.ImageItem;
import software.openlab.catalog.domain.product.ProductId;
import software.openlab.catalog.domain.product.ProductImage;
import software.openlab.catalog.domain.product.ProductImageId;
import software.openlab.catalog.domain.product.ProductImageRepository;
import software.openlab.catalog.domain.shared.DomainEvent;
import software.openlab.catalog.domain.shared.NotFoundException;

@ApplicationScoped
public class SetPrimaryProductImageUseCase {

    @Inject
    ProductImageRepository repository;

    @Inject
    Event<DomainEvent> events;

    @Inject
    GetProductByIdUseCase getProductById;

    @Transactional
    public void execute(ProductId productId, ProductImageId imageId) {
        getProductById.execute(productId);

        ProductImage image = repository.findById(imageId)
                .filter(i -> i.getProductId().equals(productId))
                .orElseThrow(() -> new NotFoundException("error.image.notFound", imageId));

        repository.clearPrimaryForProduct(productId);
        repository.setPrimary(image.getImageId());

        List<ImageItem> current = repository.findByProductId(productId).stream().map(ImageItem::from).toList();
        events.fire(new DomainEvent(ProductEvents.UPDATED,
                new ProductEvents.ProductImagesUpdatedPayload(productId.toString(), current)));
    }
}
