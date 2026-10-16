package software.openlab.crm.domain.stage;

import software.openlab.crm.domain.shared.BadRequestException;
import software.openlab.crm.domain.shared.Ulid;

/** Public identifier: {@code stage_<ULID>}. */
public record StageId(String value) {

    private static final String PREFIXED = "stage_";

    public StageId {
        if (value == null || !value.startsWith(PREFIXED) || !Ulid.isValid(value.substring(PREFIXED.length()))) {
            throw new BadRequestException("error.id.invalid", PREFIXED + "<ULID>");
        }
    }

    public static StageId generate() {
        return new StageId(PREFIXED + Ulid.generate());
    }

    public static StageId of(String raw) {
        return new StageId(raw);
    }

    @Override
    public String toString() {
        return value;
    }
}
