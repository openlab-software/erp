package software.openlab.customer.domain.customer;

public final class CustomerEvents {

    public static final String CREATED = "customer.created";
    public static final String UPDATED = "customer.updated";
    public static final String DELETED = "customer.deleted";

    private CustomerEvents() {
    }

    /** Shared by created/updated: other services only need the identity and the status. */
    public record CustomerPayload(String id, String type, String status, String name, String document) {
        public static CustomerPayload of(Customer c) {
            return new CustomerPayload(c.getCustomerId().toString(), c.getType().name(), c.getStatus().name(),
                    c.getName(), c.getDocument());
        }
    }

    public record CustomerDeletedPayload(String id) {
    }
}
