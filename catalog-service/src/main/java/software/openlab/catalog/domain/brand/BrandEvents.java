package software.openlab.catalog.domain.brand;

public final class BrandEvents {

    public static final String CREATED = "brand.created";
    public static final String UPDATED = "brand.updated";
    public static final String DELETED = "brand.deleted";

    private BrandEvents() {
    }

    public record BrandCreatedPayload(String id, String description) {
    }

    public record BrandUpdatedPayload(String id, String description) {
    }

    public record BrandDeletedPayload(String id) {
    }
}
