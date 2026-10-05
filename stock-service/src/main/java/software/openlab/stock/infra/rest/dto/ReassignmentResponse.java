package software.openlab.stock.infra.rest.dto;

import java.time.Instant;
import java.util.List;
import software.openlab.stock.domain.reassignment.Reassignment;

public record ReassignmentResponse(String reassignmentId, String fromStockId, String toStockId,
                                    List<ReassignmentItemResponse> items, Instant createdAt) {

    public static ReassignmentResponse from(Reassignment reassignment) {
        List<ReassignmentItemResponse> items = reassignment.getItems().stream()
                .map(ReassignmentItemResponse::from)
                .toList();
        return new ReassignmentResponse(
                reassignment.getReassignmentId().toString(),
                reassignment.getFromStockId().toString(),
                reassignment.getToStockId().toString(),
                items,
                reassignment.getCreatedAt());
    }
}
