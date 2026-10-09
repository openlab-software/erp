package software.openlab.customer.domain.shared;

/** Small validation helper shared by the create/update use cases. */
public final class Fields {

    private Fields() {
    }

    public static String requireNonBlank(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new BadRequestException("error.field.blank", field);
        }
        return value.trim();
    }
}
