package software.openlab.crm.infra.rest.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;
import software.openlab.crm.domain.stage.Stage;

public final class StageDTOs {

    private StageDTOs() {
    }

    public record CreateStageRequest(@NotBlank String name, Integer position, @Min(0) @Max(100) Integer probability,
                                     Boolean won) {
    }

    public record UpdateStageRequest(@NotBlank String name, @Min(0) int position, @Min(0) @Max(100) int probability,
                                     boolean won) {
    }

    public record StageResponse(String stageId, String name, int position, int probability, boolean won,
                                Instant createdAt, Instant updatedAt) {
        public static StageResponse from(Stage s) {
            return new StageResponse(s.getStageId().toString(), s.getName(), s.getPosition(), s.getProbability(),
                    s.isWon(), s.getCreatedAt(), s.getUpdatedAt());
        }
    }

    /** Not paginated: a pipeline has a handful of stages. */
    public record StageListResponse(List<StageResponse> data) {
        public static StageListResponse from(List<Stage> stages) {
            return new StageListResponse(stages.stream().map(StageResponse::from).toList());
        }
    }
}
