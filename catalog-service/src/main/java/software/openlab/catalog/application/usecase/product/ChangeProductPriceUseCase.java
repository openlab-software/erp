package software.openlab.catalog.application.usecase.product;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import software.openlab.catalog.domain.product.PriceHistory;
import software.openlab.catalog.domain.product.PriceHistoryRepository;
import software.openlab.catalog.domain.product.Product;
import software.openlab.catalog.domain.product.ProductEvents;
import software.openlab.catalog.domain.product.ProductId;
import software.openlab.catalog.domain.product.ProductRepository;
import software.openlab.catalog.domain.shared.BadRequestException;
import software.openlab.catalog.domain.shared.DomainEvent;

@ApplicationScoped
public class ChangeProductPriceUseCase {

    @Inject
    ProductRepository repository;

    @Inject
    PriceHistoryRepository priceHistoryRepository;

    @Inject
    Event<DomainEvent> events;

    @Inject
    GetProductByIdUseCase getProductById;

    @Transactional
    public Product execute(ProductId productId, BigDecimal salePrice, BigDecimal costPrice) {
        if (salePrice == null || salePrice.signum() < 0) {
            throw new BadRequestException("error.field.negative", "sale_price");
        }
        if (costPrice == null || costPrice.signum() < 0) {
            throw new BadRequestException("error.field.negative", "cost_price");
        }

        Product product = getProductById.execute(productId);
        product.setSalePrice(salePrice);
        product.setCostPrice(costPrice);
        repository.update(product);

        priceHistoryRepository.insert(PriceHistory.newPriceHistory(productId, salePrice, costPrice));

        events.fire(new DomainEvent(ProductEvents.PRICE_CHANGED,
                new ProductEvents.ProductPriceChangedPayload(productId.toString(), salePrice, costPrice)));

        return getProductById.execute(productId);
    }
}
