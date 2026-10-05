package software.openlab.catalog.domain.product;

import java.math.BigDecimal;
import java.time.Instant;

public record PriceHistory(PriceHistoryId priceHistoryId, ProductId productId, BigDecimal salePrice,
                            BigDecimal costPrice, Instant changedAt) {

    public static PriceHistory newPriceHistory(ProductId productId, BigDecimal salePrice, BigDecimal costPrice) {
        return new PriceHistory(PriceHistoryId.generate(), productId, salePrice, costPrice, Instant.now());
    }
}
