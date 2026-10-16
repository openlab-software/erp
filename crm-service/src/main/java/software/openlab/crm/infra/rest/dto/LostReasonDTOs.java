package software.openlab.crm.infra.rest.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;
import software.openlab.crm.domain.lostreason.LostReason;

public final class LostReasonDTOs {

    private LostReasonDTOs() {
    }

    public record CreateLostReasonRequest(@NotBlank String name) {
    }

    public record UpdateLostReasonRequest(@NotBlank String name, Boolean active) {
    }

    public record LostReasonResponse(String lostReasonId, String name, boolean active, Instant createdAt,
                                     Instant updatedAt) {
        public static LostReasonResponse from(LostReason r) {
            return new LostReasonResponse(r.getLostReasonId().toString(), r.getName(), r.isActive(),
                    r.getCreatedAt(), r.getUpdatedAt());
        }
    }

    public record LostReasonListResponse(List<LostReasonResponse> data) {
        public static LostReasonListResponse from(List<LostReason> reasons) {
            return new LostReasonListResponse(reasons.stream().map(LostReasonResponse::from).toList());
        }
    }
}
