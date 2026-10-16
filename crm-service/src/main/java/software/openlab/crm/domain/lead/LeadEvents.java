package software.openlab.crm.domain.lead;

import java.math.BigDecimal;

public final class LeadEvents {

    public static final String CREATED = "lead.created";
    public static final String UPDATED = "lead.updated";
    public static final String STAGE_CHANGED = "lead.stage_changed";
    public static final String WON = "lead.won";
    public static final String LOST = "lead.lost";
    public static final String REOPENED = "lead.reopened";
    public static final String DELETED = "lead.deleted";

    private LeadEvents() {
    }

    public record LeadPayload(String id, String name, String type, String status, String stageId, String customerId,
                              String contactName, String email, BigDecimal expectedRevenue, int probability,
                              String assignedTo) {
        public static LeadPayload of(Lead l) {
            return new LeadPayload(l.getLeadId().toString(), l.getName(), l.getType().name(), l.getStatus().name(),
                    l.getStageId().toString(), l.getCustomerId(), l.getContactName(), l.getEmail(),
                    l.getExpectedRevenue(), l.getProbability(), l.getAssignedTo());
        }
    }

    public record LeadDeletedPayload(String id) {
    }
}
