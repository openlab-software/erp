package software.openlab.customer.domain.address;

public final class AddressEvents {

    public static final String CREATED = "address.created";
    public static final String UPDATED = "address.updated";
    public static final String DELETED = "address.deleted";

    private AddressEvents() {
    }

    public record AddressPayload(String id, String customerId, String label, String zipCode, String city,
                                 String state, boolean isDefault) {
        public static AddressPayload of(String customerId, Address a) {
            return new AddressPayload(a.addressId().toString(), customerId, a.label(), a.zipCode(), a.city(),
                    a.state(), a.isDefault());
        }
    }

    public record AddressDeletedPayload(String id, String customerId) {
    }
}
