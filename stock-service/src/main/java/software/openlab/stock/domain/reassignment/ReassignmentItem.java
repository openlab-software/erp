package software.openlab.stock.domain.reassignment;

import java.time.Instant;
import lombok.Getter;
import software.openlab.stock.domain.shared.ProductId;

@Getter
public final class ReassignmentItem {

    private final ProductId productId;
    private final int quantity;
    private final Instant createdAt;

    public ReassignmentItem(ProductId productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
        this.createdAt = Instant.now();
    }
}
