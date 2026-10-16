package software.openlab.crm.domain.lead;

import software.openlab.crm.domain.shared.BadRequestException;
import software.openlab.crm.domain.shared.Ulid;

/** Public identifier: {@code lead_<ULID>}. */
public record LeadId(String value) {

    private static final String PREFIXED = "lead_";

    public LeadId {
        if (value == null || !value.startsWith(PREFIXED) || !Ulid.isValid(value.substring(PREFIXED.length()))) {
            throw new BadRequestException("error.id.invalid", PREFIXED + "<ULID>");
        }
    }

    public static LeadId generate() {
        return new LeadId(PREFIXED + Ulid.generate());
    }

    public static LeadId of(String raw) {
        return new LeadId(raw);
    }

    @Override
    public String toString() {
        return value;
    }
}
