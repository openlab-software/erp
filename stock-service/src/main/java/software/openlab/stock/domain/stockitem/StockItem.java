package software.openlab.stock.domain.stockitem;

import software.openlab.stock.domain.shared.ProductId;
import software.openlab.stock.domain.stock.StockId;

/**
 * Read model for a {@code Stock_Item} balance. {@code reservedValue} is always {@code 0} for
 * now — the column is introduced by a later task (Requirement 6 of {@code stock-master-data});
 * until then {@link #availableValue()} is always equal to {@code currentValue}.
 */
public record StockItem(
        StockId stockId,
        ProductId productId,
        Integer minValue,
        int currentValue,
        Integer maxValue,
        int reservedValue,
        boolean active
) {

    public int availableValue() {
        return currentValue - reservedValue;
    }
}
