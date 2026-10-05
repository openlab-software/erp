package software.openlab.stock.infra.rest.dto;

import java.time.Instant;
import software.openlab.stock.domain.movementreason.MovementReason;

public record MovementReasonResponse(String reasonId, String description, Instant createdAt) {

    public static MovementReasonResponse from(MovementReason reason) {
        return new MovementReasonResponse(reason.getReasonId().toString(), reason.getDescription(),
                reason.getCreatedAt());
    }
}
