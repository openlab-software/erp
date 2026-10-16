package software.openlab.crm.domain.activity;

import software.openlab.crm.domain.shared.BadRequestException;

public enum ActivityType {
    TODO,
    CALL,
    MEETING,
    EMAIL;

    public static ActivityType parse(String raw) {
        try {
            return valueOf(raw.trim().toUpperCase());
        } catch (RuntimeException e) {
            throw new BadRequestException("error.field.invalid", "type", raw);
        }
    }
}
