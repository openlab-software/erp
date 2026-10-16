package software.openlab.crm.domain.activity;

import software.openlab.crm.domain.shared.BadRequestException;
import software.openlab.crm.domain.shared.Ulid;

/** Public identifier: {@code activity_<ULID>}. */
public record ActivityId(String value) {

    private static final String PREFIXED = "activity_";

    public ActivityId {
        if (value == null || !value.startsWith(PREFIXED) || !Ulid.isValid(value.substring(PREFIXED.length()))) {
            throw new BadRequestException("error.id.invalid", PREFIXED + "<ULID>");
        }
    }

    public static ActivityId generate() {
        return new ActivityId(PREFIXED + Ulid.generate());
    }

    public static ActivityId of(String raw) {
        return new ActivityId(raw);
    }

    @Override
    public String toString() {
        return value;
    }
}
