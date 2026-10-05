package software.openlab.catalog.application.usecase.product;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.catalog.domain.product.Product;
import software.openlab.catalog.domain.product.ProductEvents;
import software.openlab.catalog.domain.product.ProductId;
import software.openlab.catalog.domain.product.ProductRepository;
import software.openlab.catalog.domain.product.ProductStatus;
import software.openlab.catalog.domain.shared.DomainEvent;

@ApplicationScoped
public class ChangeProductStatusUseCase {

    @Inject
    ProductRepository repository;

    @Inject
    Event<DomainEvent> events;

    @Inject
    GetProductByIdUseCase getProductById;

    @Transactional
    public Product execute(ProductId productId, String status) {
        ProductStatus newStatus = ProductStatus.parse(status);

        Product product = getProductById.execute(productId);
        product.getStatus().requireTransitionTo(newStatus);

        product.setStatus(newStatus);
        repository.update(product);

        Product updated = getProductById.execute(productId);
        events.fire(new DomainEvent(ProductEvents.UPDATED, ProductEvents.updatedPayloadOf(updated)));

        return updated;
    }
}
