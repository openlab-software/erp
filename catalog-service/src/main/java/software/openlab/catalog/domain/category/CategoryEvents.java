package software.openlab.catalog.domain.category;

public final class CategoryEvents {

    public static final String CREATED = "category.created";
    public static final String UPDATED = "category.updated";
    public static final String DELETED = "category.deleted";

    private CategoryEvents() {
    }

    public record CategoryCreatedPayload(String id, String description, String parentCategoryId) {
    }

    public record CategoryUpdatedPayload(String id, String description, String parentCategoryId) {
    }

    public record CategoryDeletedPayload(String id) {
    }
}
