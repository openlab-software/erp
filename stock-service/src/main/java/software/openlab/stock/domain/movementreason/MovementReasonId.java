package software.openlab.stock.domain.movementreason;

import software.openlab.stock.domain.shared.PrefixedId;

/** Strongly-typed MovementReason aggregate id, format {@code reason_<ULID>}. */
public record MovementReasonId(String value) {

    public static final String PREFIX = "reason";

    public MovementReasonId {
        value = PrefixedId.parse(PREFIX, value);
    }

    public static MovementReasonId generate() {
        return new MovementReasonId(PrefixedId.generate(PREFIX));
    }

    public static MovementReasonId of(String raw) {
        return new MovementReasonId(raw);
    }

    @Override
    public String toString() {
        return value;
    }
}
