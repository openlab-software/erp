package software.openlab.stock.application.usecase.stock;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.stock.domain.shared.ProductId;
import software.openlab.stock.domain.stock.StockRepository;

/** product.deleted — removes StockItems associated with the deleted product. */
@ApplicationScoped
public class HandleProductDeletedUseCase {

    @Inject
    StockRepository stockRepository;

    @Transactional
    public void execute(ProductId productId) {
        stockRepository.deleteItemsForProduct(productId);
    }
}
