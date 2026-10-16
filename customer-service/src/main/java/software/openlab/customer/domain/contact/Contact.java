package software.openlab.customer.domain.contact;

import java.util.regex.Pattern;
import software.openlab.customer.domain.shared.BadRequestException;
import software.openlab.customer.domain.shared.Fields;

/**
 * A person to talk to at a customer (the buyer, the finance contact, ...), like the child contacts of a company
 * in Odoo. A customer has up to {@link #MAX_PER_CUSTOMER} of them and at most one is the primary contact.
 *
 * <p>This module only knows the customer by its id (see {@link ContactRepository}); whether the customer exists
 * is checked by the use cases.
 */
public record Contact(
        ContactId contactId,
        String name,
        String jobTitle,
        String email,
        String phone,
        String notes,
        boolean isPrimary) {

    public static final int MAX_PER_CUSTOMER = 20;

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /**
     * Trims, drops blanks and checks the name and e-mail. The result keeps {@code id} (the contact being edited)
     * or gets a new one when {@code id} is {@code null}.
     */
    public static Contact validated(ContactId id, Contact raw) {
        String mail = blankToNull(raw.email());
        if (mail != null && !EMAIL.matcher(mail).matches()) {
            throw new BadRequestException("error.customer.email.invalid", mail);
        }
        return new Contact(id != null ? id : ContactId.generate(), Fields.requireNonBlank(raw.name(), "name"),
                blankToNull(raw.jobTitle()), mail, blankToNull(raw.phone()), blankToNull(raw.notes()),
                raw.isPrimary());
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
