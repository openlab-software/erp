package software.openlab.stock.domain.reassignment;

import software.openlab.stock.domain.shared.PrefixedId;

/** Strongly-typed Reassignment aggregate id, format {@code stock_reassignment_<ULID>}. */
public record ReassignmentId(String value) {

    public static final String PREFIX = "stock_reassignment";

    public ReassignmentId {
        value = PrefixedId.parse(PREFIX, value);
    }

    public static ReassignmentId generate() {
        return new ReassignmentId(PrefixedId.generate(PREFIX));
    }

    public static ReassignmentId of(String raw) {
        return new ReassignmentId(raw);
    }

    @Override
    public String toString() {
        return value;
    }
}
