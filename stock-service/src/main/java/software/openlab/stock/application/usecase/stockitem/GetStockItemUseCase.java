package software.openlab.stock.application.usecase.stockitem;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.openlab.stock.application.usecase.stock.GetStockByIdUseCase;
import software.openlab.stock.domain.shared.NotFoundException;
import software.openlab.stock.domain.shared.ProductId;
import software.openlab.stock.domain.stock.StockId;
import software.openlab.stock.domain.stockitem.StockItem;
import software.openlab.stock.domain.stockitem.StockItemRepository;

@ApplicationScoped
public class GetStockItemUseCase {

    @Inject
    StockItemRepository stockItemRepository;

    @Inject
    GetStockByIdUseCase getStockById;

    public StockItem execute(StockId stockId, ProductId productId) {
        getStockById.execute(stockId);
        return stockItemRepository.findByStockAndProduct(stockId, productId)
                .orElseThrow(() -> new NotFoundException(
                        "error.stockItem.notFound"));
    }
}
