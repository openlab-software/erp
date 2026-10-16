package software.openlab.customer.domain.contact;

public final class ContactEvents {

    public static final String CREATED = "contact.created";
    public static final String UPDATED = "contact.updated";
    public static final String DELETED = "contact.deleted";

    private ContactEvents() {
    }

    public record ContactPayload(String id, String customerId, String name, String jobTitle, String email,
                                 boolean isPrimary) {
        public static ContactPayload of(String customerId, Contact c) {
            return new ContactPayload(c.contactId().toString(), customerId, c.name(), c.jobTitle(), c.email(),
                    c.isPrimary());
        }
    }

    public record ContactDeletedPayload(String id, String customerId) {
    }
}
