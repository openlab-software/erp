package software.openlab.catalog.infra.rest.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;
import software.openlab.catalog.domain.brand.Brand;
import software.openlab.catalog.domain.shared.PageResult;

public final class BrandDTOs {

    private BrandDTOs() {
    }

    public record CreateBrandRequest(@NotBlank String description) {
    }

    public record UpdateBrandRequest(@NotBlank String description) {
    }

    public record BrandResponse(String brandId, String description, Instant createdAt, Instant updatedAt) {
        public static BrandResponse from(Brand b) {
            return new BrandResponse(b.getBrandId().toString(), b.getDescription(), b.getCreatedAt(), b.getUpdatedAt());
        }
    }

    public record BrandPageResponse(List<BrandResponse> data, int page, int pageSize, long total) {
        public static BrandPageResponse from(PageResult<Brand> page) {
            List<BrandResponse> data = page.data().stream().map(BrandResponse::from).toList();
            return new BrandPageResponse(data, page.page(), page.pageSize(), page.total());
        }
    }
}
