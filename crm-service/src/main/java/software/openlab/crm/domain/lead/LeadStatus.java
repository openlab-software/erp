package software.openlab.crm.domain.lead;

import software.openlab.crm.domain.shared.BadRequestException;

public enum LeadStatus {
    OPEN,
    WON,
    LOST;

    public static LeadStatus parse(String raw) {
        try {
            return valueOf(raw.trim().toUpperCase());
        } catch (RuntimeException e) {
            throw new BadRequestException("error.field.invalid", "status", raw);
        }
    }
}
