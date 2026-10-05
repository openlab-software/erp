package software.openlab.catalog.application.usecase.product;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import software.openlab.catalog.domain.product.ProductEvents;
import software.openlab.catalog.domain.product.ProductEvents.ImageItem;
import software.openlab.catalog.domain.product.ProductId;
import software.openlab.catalog.domain.product.ProductImage;
import software.openlab.catalog.domain.product.ProductImageRepository;
import software.openlab.catalog.domain.shared.BadRequestException;
import software.openlab.catalog.domain.shared.DomainEvent;
import software.openlab.catalog.domain.shared.Fields;

@ApplicationScoped
public class AddProductImageUseCase {

    @Inject
    ProductImageRepository repository;

    @Inject
    Event<DomainEvent> events;

    @Inject
    GetProductByIdUseCase getProductById;

    @Transactional
    public ProductImage execute(ProductId productId, String url) {
        getProductById.execute(productId);

        String trimmedUrl = Fields.requireNonBlank(url, "url");
        if (!isWellFormedUrl(trimmedUrl)) {
            throw new BadRequestException("error.image.invalidUrl");
        }

        boolean isFirstImage = repository.countByProductId(productId) == 0;
        ProductImage image = ProductImage.newProductImage(productId, trimmedUrl, isFirstImage);
        repository.insert(image);

        List<ImageItem> current = repository.findByProductId(productId).stream().map(ImageItem::from).toList();
        events.fire(new DomainEvent(ProductEvents.UPDATED,
                new ProductEvents.ProductImagesUpdatedPayload(productId.toString(), current)));

        return image;
    }

    private boolean isWellFormedUrl(String value) {
        try {
            URI uri = new URI(value);
            return uri.isAbsolute() && uri.getHost() != null;
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
