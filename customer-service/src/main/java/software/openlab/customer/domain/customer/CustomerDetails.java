package software.openlab.customer.domain.customer;

import java.time.LocalDate;
import java.util.regex.Pattern;
import software.openlab.customer.domain.shared.BadRequestException;
import software.openlab.customer.domain.shared.Fields;

/**
 * The editable, already-validated data of a customer. Create and update both go through
 * {@link #validated}, so the type-specific rules live in one place:
 * individuals have no trade name / state registration, companies have no birth date.
 */
public record CustomerDetails(
        String name,
        String tradeName,
        String document,
        String stateRegistration,
        LocalDate birthDate,
        String email,
        String phone) {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public static CustomerDetails validated(CustomerType type, String name, String tradeName, String document,
                                            String stateRegistration, LocalDate birthDate, String email,
                                            String phone) {
        String trimmedName = Fields.requireNonBlank(name, "name");
        String digits = Document.normalize(type, document);

        String trimmedEmail = blankToNull(email);
        if (trimmedEmail != null && !EMAIL.matcher(trimmedEmail).matches()) {
            throw new BadRequestException("error.customer.email.invalid", trimmedEmail);
        }

        boolean company = type == CustomerType.COMPANY;
        if (!company && birthDate != null && birthDate.isAfter(LocalDate.now())) {
            throw new BadRequestException("error.customer.birthDate.future");
        }

        return new CustomerDetails(
                trimmedName,
                company ? blankToNull(tradeName) : null,
                digits,
                company ? blankToNull(stateRegistration) : null,
                company ? null : birthDate,
                trimmedEmail,
                blankToNull(phone));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
