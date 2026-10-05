package software.openlab.catalog.infra.rest.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;
import software.openlab.catalog.domain.shared.PageResult;
import software.openlab.catalog.domain.unitofmeasure.UnitOfMeasure;

public final class UnitOfMeasureDTOs {

    private UnitOfMeasureDTOs() {
    }

    public record CreateUnitOfMeasureRequest(@NotBlank String code, @NotBlank String description) {
    }

    public record UpdateUnitOfMeasureRequest(@NotBlank String code, @NotBlank String description) {
    }

    public record UnitOfMeasureResponse(String unitOfMeasureId, String code, String description, Instant createdAt, Instant updatedAt) {
        public static UnitOfMeasureResponse from(UnitOfMeasure u) {
            return new UnitOfMeasureResponse(u.getUnitOfMeasureId().toString(), u.getCode(), u.getDescription(), u.getCreatedAt(), u.getUpdatedAt());
        }
    }

    public record UnitOfMeasurePageResponse(List<UnitOfMeasureResponse> data, int page, int pageSize, long total) {
        public static UnitOfMeasurePageResponse from(PageResult<UnitOfMeasure> page) {
            List<UnitOfMeasureResponse> data = page.data().stream().map(UnitOfMeasureResponse::from).toList();
            return new UnitOfMeasurePageResponse(data, page.page(), page.pageSize(), page.total());
        }
    }
}
