package software.openlab.stock.infra.rest.dto;

import java.time.Instant;
import software.openlab.stock.domain.stock.Stock;

public record StockResponse(String stockId, String description, Instant createdAt, Instant modifiedAt) {

    public static StockResponse from(Stock stock) {
        return new StockResponse(stock.getStockId().toString(), stock.getDescription(),
                stock.getCreatedAt(), stock.getModifiedAt());
    }
}
