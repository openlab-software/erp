package software.openlab.stock.domain.shared;

/**
 * Shared parsing/formatting logic for the {@code {prefix}_{ULID}} id records ({@code StockId},
 * {@code ReassignmentId}, {@code ProductId}). Each record's compact constructor delegates here
 * so the validation rule lives in one place while the public type stays specific to its
 * aggregate. Public because StockId/ReassignmentId live in sibling packages (domain.stock /
 * domain.reassignment) and need to call it too.
 */
public final class PrefixedId {

    private PrefixedId() {
    }

    public static String generate(String prefix) {
        return prefix + "_" + Ulid.generate();
    }

    /** Validates {@code raw} against {@code prefix + "_" + ULID} and returns the canonical form. */
    public static String parse(String prefix, String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BadRequestException("error.id.missing");
        }

        String marker = prefix + "_";
        if (!raw.startsWith(marker)) {
            throw new BadRequestException("error.id.invalid", prefix + "_<ULID>");
        }

        String ulidPart = raw.substring(marker.length()).toUpperCase();
        if (!Ulid.isValid(ulidPart)) {
            throw new BadRequestException("error.id.invalid", prefix + "_<ULID>");
        }

        return prefix + "_" + ulidPart;
    }
}
