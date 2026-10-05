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

/**
 * Deletes the product following its lifecycle rule (Requirement 7): hard-delete
 * while DRAFT, soft-delete (-&gt; INACTIVE) while PUBLISHED/ACTIVE, no-op if already INACTIVE.
 */
@ApplicationScoped
public class DeleteProductUseCase {

    @Inject
    ProductRepository repository;

    @Inject
    Event<DomainEvent> events;

    @Inject
    GetProductByIdUseCase getProductById;

    @Transactional
    public void execute(ProductId productId) {
        Product product = getProductById.execute(productId);

        switch (product.getStatus()) {
            case DRAFT -> {
                repository.deleteById(productId);
                events.fire(new DomainEvent(ProductEvents.DELETED, new ProductEvents.ProductDeletedPayload(productId.toString())));
            }
            case PUBLISHED, ACTIVE -> {
                product.setStatus(ProductStatus.INACTIVE);
                repository.update(product);
                Product updated = getProductById.execute(productId);
                events.fire(new DomainEvent(ProductEvents.UPDATED, ProductEvents.updatedPayloadOf(updated)));
            }
            case INACTIVE -> {
                // already inactive: no-op, still 204 (Requirement 7.3)
            }
        }
    }
}
