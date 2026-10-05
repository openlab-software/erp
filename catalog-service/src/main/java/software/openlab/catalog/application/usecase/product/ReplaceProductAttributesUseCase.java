package software.openlab.catalog.application.usecase.product;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import software.openlab.catalog.domain.product.ProductAttribute;
import software.openlab.catalog.domain.product.ProductAttributeRepository;
import software.openlab.catalog.domain.product.ProductEvents;
import software.openlab.catalog.domain.product.ProductId;
import software.openlab.catalog.domain.shared.BadRequestException;
import software.openlab.catalog.domain.shared.DomainEvent;

@ApplicationScoped
public class ReplaceProductAttributesUseCase {

    @Inject
    ProductAttributeRepository repository;

    @Inject
    Event<DomainEvent> events;

    @Inject
    GetProductByIdUseCase getProductById;

    @Transactional
    public List<ProductAttribute> execute(ProductId productId, List<ProductAttribute> rawAttributes) {
        getProductById.execute(productId);

        List<ProductAttribute> trimmed = new ArrayList<>();
        Set<String> seenNames = new HashSet<>();

        for (ProductAttribute raw : rawAttributes) {
            String name = raw.name() == null ? "" : raw.name().trim();
            String value = raw.value() == null ? "" : raw.value().trim();

            if (name.isEmpty() || value.isEmpty()) {
                throw new BadRequestException("error.attribute.blank");
            }
            if (!seenNames.add(name.toLowerCase())) {
                throw new BadRequestException("error.attribute.duplicate", name);
            }

            trimmed.add(new ProductAttribute(name, value));
        }

        repository.replaceAll(productId, trimmed);

        events.fire(new DomainEvent(ProductEvents.UPDATED,
                new ProductEvents.ProductAttributesUpdatedPayload(productId.toString(), trimmed)));

        return trimmed;
    }
}
