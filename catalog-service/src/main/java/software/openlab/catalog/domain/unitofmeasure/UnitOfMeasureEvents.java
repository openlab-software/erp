package software.openlab.catalog.domain.unitofmeasure;

public final class UnitOfMeasureEvents {

    public static final String CREATED = "uom.created";
    public static final String UPDATED = "uom.updated";
    public static final String DELETED = "uom.deleted";

    private UnitOfMeasureEvents() {
    }

    public record UnitOfMeasureCreatedPayload(String id, String code, String description) {
    }

    public record UnitOfMeasureUpdatedPayload(String id, String code, String description) {
    }

    public record UnitOfMeasureDeletedPayload(String id) {
    }
}
