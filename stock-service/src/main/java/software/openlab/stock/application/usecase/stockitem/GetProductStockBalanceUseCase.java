package software.openlab.stock.application.usecase.stockitem;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import software.openlab.stock.domain.shared.ProductId;
import software.openlab.stock.domain.stock.StockId;
import software.openlab.stock.domain.stockitem.StockItem;
import software.openlab.stock.domain.stockitem.StockItemRepository;

/**
 * GET /v1/products/{productId}/balance — Requirement 9. Sums {@code current_value}/
 * {@code reserved_value} across every {@code active=true} Stock_Item of this product in every
 * stock. A product with no Stock_Item anywhere is not an error — it simply yields all totals at
 * zero and an empty {@code by_stock} (Requirement 9.2).
 */
@ApplicationScoped
public class GetProductStockBalanceUseCase {

    @Inject
    StockItemRepository stockItemRepository;

    public ProductStockBalance execute(ProductId productId) {
        List<StockItem> items = stockItemRepository.findAllByProduct(productId);

        int totalCurrentValue = 0;
        int totalReservedValue = 0;
        List<StockBalance> byStock = new ArrayList<>();
        for (StockItem item : items) {
            totalCurrentValue += item.currentValue();
            totalReservedValue += item.reservedValue();
            byStock.add(new StockBalance(item.stockId(), item.currentValue(), item.reservedValue(),
                    item.availableValue()));
        }

        return new ProductStockBalance(productId, totalCurrentValue, totalReservedValue,
                totalCurrentValue - totalReservedValue, byStock);
    }

    public record StockBalance(StockId stockId, int currentValue, int reservedValue, int availableValue) {
    }

    public record ProductStockBalance(ProductId productId, int totalCurrentValue, int totalReservedValue,
                                       int totalAvailableValue, List<StockBalance> byStock) {
    }
}
