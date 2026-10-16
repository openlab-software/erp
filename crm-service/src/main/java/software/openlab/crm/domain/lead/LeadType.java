package software.openlab.crm.domain.lead;

import software.openlab.crm.domain.shared.BadRequestException;

/** A LEAD is an unqualified contact; qualifying it turns it into an OPPORTUNITY on the pipeline. */
public enum LeadType {
    LEAD,
    OPPORTUNITY;

    public static LeadType parse(String raw) {
        try {
            return valueOf(raw.trim().toUpperCase());
        } catch (RuntimeException e) {
            throw new BadRequestException("error.field.invalid", "type", raw);
        }
    }
}
