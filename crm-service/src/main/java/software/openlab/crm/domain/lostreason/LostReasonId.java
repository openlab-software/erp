package software.openlab.crm.domain.lostreason;

import software.openlab.crm.domain.shared.BadRequestException;
import software.openlab.crm.domain.shared.Ulid;

/** Public identifier: {@code lost_reason_<ULID>}. */
public record LostReasonId(String value) {

    private static final String PREFIXED = "lost_reason_";

    public LostReasonId {
        if (value == null || !value.startsWith(PREFIXED) || !Ulid.isValid(value.substring(PREFIXED.length()))) {
            throw new BadRequestException("error.id.invalid", PREFIXED + "<ULID>");
        }
    }

    public static LostReasonId generate() {
        return new LostReasonId(PREFIXED + Ulid.generate());
    }

    public static LostReasonId of(String raw) {
        return new LostReasonId(raw);
    }

    @Override
    public String toString() {
        return value;
    }
}
