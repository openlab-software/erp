package software.openlab.crm.domain.lostreason;

public final class LostReasonEvents {

    public static final String CREATED = "lost_reason.created";
    public static final String UPDATED = "lost_reason.updated";
    public static final String DELETED = "lost_reason.deleted";

    private LostReasonEvents() {
    }

    public record LostReasonPayload(String id, String name, boolean active) {
        public static LostReasonPayload of(LostReason r) {
            return new LostReasonPayload(r.getLostReasonId().toString(), r.getName(), r.isActive());
        }
    }

    public record LostReasonDeletedPayload(String id) {
    }
}
