package software.openlab.stock.infra.rest.dto;

import software.openlab.stock.domain.stockitem.StockItem;

public record StockItemResponse(String stockId, String productId, Integer minValue, int currentValue,
                                 Integer maxValue, int reservedValue, int availableValue, boolean active) {

    public static StockItemResponse from(StockItem item) {
        return new StockItemResponse(item.stockId().toString(), item.productId().toString(), item.minValue(),
                item.currentValue(), item.maxValue(), item.reservedValue(), item.availableValue(), item.active());
    }
}
