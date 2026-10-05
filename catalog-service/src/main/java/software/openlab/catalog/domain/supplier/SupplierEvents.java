package software.openlab.catalog.domain.supplier;

public final class SupplierEvents {

    public static final String CREATED = "supplier.created";
    public static final String UPDATED = "supplier.updated";
    public static final String DELETED = "supplier.deleted";

    private SupplierEvents() {
    }

    public record SupplierCreatedPayload(String id, String name, String document) {
    }

    public record SupplierUpdatedPayload(String id, String name, String document) {
    }

    public record SupplierDeletedPayload(String id) {
    }
}
