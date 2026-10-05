package software.openlab.stock.application.usecase.stock;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.stock.domain.shared.ProductId;
import software.openlab.stock.domain.stock.StockRepository;

/** product.created — creates an empty StockItem for the new product in every existing stock. */
@ApplicationScoped
public class HandleProductCreatedUseCase {

    @Inject
    StockRepository stockRepository;

    @Transactional
    public void execute(ProductId productId) {
        stockRepository.createEmptyItemsForProductInAllStocks(productId);
    }
}
