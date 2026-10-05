package software.openlab.stock.infra.rest.dto;

import java.time.Instant;
import software.openlab.stock.domain.stockcount.StockCount;

public record StockCountResponse(String countId, String stockId, String productId, int systemValue,
                                  int countedValue, Instant createdAt) {

    public static StockCountResponse from(StockCount count) {
        return new StockCountResponse(count.getCountId().toString(), count.getStockId().toString(),
                count.getProductId().toString(), count.getSystemValue(), count.getCountedValue(),
                count.getCreatedAt());
    }
}
