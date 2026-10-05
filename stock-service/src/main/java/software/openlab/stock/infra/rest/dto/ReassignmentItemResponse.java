package software.openlab.stock.infra.rest.dto;

import java.time.Instant;
import software.openlab.stock.domain.reassignment.ReassignmentItem;

public record ReassignmentItemResponse(String productId, int quantity, Instant createdAt) {

    public static ReassignmentItemResponse from(ReassignmentItem item) {
        return new ReassignmentItemResponse(item.getProductId().toString(), item.getQuantity(), item.getCreatedAt());
    }
}
