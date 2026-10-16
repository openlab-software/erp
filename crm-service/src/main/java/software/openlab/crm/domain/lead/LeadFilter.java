package software.openlab.crm.domain.lead;

import software.openlab.crm.domain.stage.StageId;

/** Listing filters; every field may be null. q matches name, contact, company or e-mail. */
public record LeadFilter(String q, LeadType type, LeadStatus status, StageId stageId, String customerId,
                         String assignedTo, String tag) {
}
