package software.openlab.stock.infra.rest.dto;

import java.util.List;
import software.openlab.stock.application.usecase.stockitem.GetProductStockBalanceUseCase.ProductStockBalance;

public record ProductStockBalanceResponse(String productId, int totalCurrentValue, int totalReservedValue,
                                           int totalAvailableValue, List<ByStock> byStock) {

    public record ByStock(String stockId, int currentValue, int reservedValue, int availableValue) {
    }

    public static ProductStockBalanceResponse from(ProductStockBalance balance) {
        List<ByStock> byStock = balance.byStock().stream()
                .map(s -> new ByStock(s.stockId().toString(), s.currentValue(), s.reservedValue(),
                        s.availableValue()))
                .toList();
        return new ProductStockBalanceResponse(balance.productId().toString(), balance.totalCurrentValue(),
                balance.totalReservedValue(), balance.totalAvailableValue(), byStock);
    }
}
