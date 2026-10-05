package software.openlab.catalog.application.usecase.product;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import software.openlab.catalog.domain.product.PriceHistory;
import software.openlab.catalog.domain.product.PriceHistoryRepository;
import software.openlab.catalog.domain.product.ProductId;

@ApplicationScoped
public class GetProductPriceHistoryUseCase {

    @Inject
    PriceHistoryRepository repository;

    @Inject
    GetProductByIdUseCase getProductById;

    public List<PriceHistory> execute(ProductId productId) {
        getProductById.execute(productId);
        return repository.findByProductId(productId);
    }
}
