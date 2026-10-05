package software.openlab.catalog.infra.rest.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;
import software.openlab.catalog.domain.category.Category;
import software.openlab.catalog.domain.shared.PageResult;

public final class CategoryDTOs {

    private CategoryDTOs() {
    }

    public record CreateCategoryRequest(@NotBlank String description, String parentCategoryId) {
    }

    public record UpdateCategoryRequest(@NotBlank String description, String parentCategoryId) {
    }

    public record CategoryResponse(String categoryId, String description, String parentCategoryId, Instant createdAt, Instant updatedAt) {
        public static CategoryResponse from(Category c) {
            return new CategoryResponse(
                    c.getCategoryId().toString(),
                    c.getDescription(),
                    c.getParentCategoryId() != null ? c.getParentCategoryId().toString() : null,
                    c.getCreatedAt(),
                    c.getUpdatedAt());
        }
    }

    public record CategoryPageResponse(List<CategoryResponse> data, int page, int pageSize, long total) {
        public static CategoryPageResponse from(PageResult<Category> page) {
            List<CategoryResponse> data = page.data().stream().map(CategoryResponse::from).toList();
            return new CategoryPageResponse(data, page.page(), page.pageSize(), page.total());
        }
    }
}
