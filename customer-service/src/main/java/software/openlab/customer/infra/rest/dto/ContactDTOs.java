package software.openlab.customer.infra.rest.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import software.openlab.customer.domain.contact.Contact;

public final class ContactDTOs {

    private ContactDTOs() {
    }

    /**
     * Request body to add or replace a contact person. {@code isPrimary} (JSON {@code is_primary}) is optional;
     * flagging a contact as primary clears the customer's previous primary contact.
     */
    public record ContactRequest(@NotBlank String name, String jobTitle, String email, String phone, String notes,
                                 Boolean isPrimary) {

        public Contact toDomain() {
            return new Contact(null, name, jobTitle, email, phone, notes, Boolean.TRUE.equals(isPrimary));
        }
    }

    public record ContactResponse(String contactId, String name, String jobTitle, String email, String phone,
                                  String notes, boolean isPrimary) {

        public static ContactResponse from(Contact c) {
            return new ContactResponse(c.contactId().toString(), c.name(), c.jobTitle(), c.email(), c.phone(),
                    c.notes(), c.isPrimary());
        }
    }

    /** Not paginated: a customer has at most {@link Contact#MAX_PER_CUSTOMER} contacts. */
    public record ContactListResponse(List<ContactResponse> data) {

        public static ContactListResponse from(List<Contact> contacts) {
            return new ContactListResponse(contacts.stream().map(ContactResponse::from).toList());
        }
    }
}
