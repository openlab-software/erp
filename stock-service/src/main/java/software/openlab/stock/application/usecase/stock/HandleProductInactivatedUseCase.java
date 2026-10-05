package software.openlab.stock.application.usecase.stock;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.stock.domain.shared.ProductId;
import software.openlab.stock.domain.stock.StockRepository;

/** product.updated with status=INACTIVE — marks StockItems inactive, preserving balance. */
@ApplicationScoped
public class HandleProductInactivatedUseCase {

    @Inject
    StockRepository stockRepository;

    @Transactional
    public void execute(ProductId productId) {
        stockRepository.deactivateItemsForProduct(productId);
    }
}
