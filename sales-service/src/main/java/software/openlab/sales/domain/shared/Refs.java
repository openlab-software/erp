package software.openlab.sales.domain.shared;

/**
 * References to aggregates that belong to other services (customer, product, ...). This service only
 * stores the public id, so the only check possible here is the {@code <prefix>_<ULID>} format.
 */
public final class Refs {

    private Refs() {
    }

    /** {@code null}/blank becomes {@code null}; anything else must be a valid {@code prefix_<ULID>}. */
    public static String optional(String prefix, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return require(prefix, value);
    }

    public static String require(String prefix, String value) {
        String trimmed = value == null ? "" : value.trim();
        String start = prefix + "_";
        if (!trimmed.startsWith(start) || !Ulid.isValid(trimmed.substring(start.length()))) {
            throw new BadRequestException("error.id.invalid", start + "<ULID>");
        }
        return trimmed;
    }
}
