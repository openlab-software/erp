package software.openlab.customer.infra.rest.dto;

import java.util.List;
import software.openlab.customer.domain.address.Address;

public final class AddressDTOs {

    private AddressDTOs() {
    }

    /**
     * Request body to add or replace an address. {@code isDefault} (JSON {@code is_default}) is optional;
     * flagging an address as the default clears the customer's previous default.
     */
    public record AddressRequest(String label, String zipCode, String street, String number, String complement,
                                 String neighborhood, String city, String state, Boolean isDefault) {

        public Address toDomain() {
            return new Address(null, label, zipCode, street, number, complement, neighborhood, city, state,
                    Boolean.TRUE.equals(isDefault));
        }
    }

    public record AddressResponse(String addressId, String label, String zipCode, String street, String number,
                                  String complement, String neighborhood, String city, String state,
                                  boolean isDefault) {

        public static AddressResponse from(Address a) {
            return new AddressResponse(a.addressId().toString(), a.label(), a.zipCode(), a.street(), a.number(),
                    a.complement(), a.neighborhood(), a.city(), a.state(), a.isDefault());
        }
    }

    /** Not paginated: a customer has at most {@link Address#MAX_PER_CUSTOMER} addresses. */
    public record AddressListResponse(List<AddressResponse> data) {

        public static AddressListResponse from(List<Address> addresses) {
            return new AddressListResponse(addresses.stream().map(AddressResponse::from).toList());
        }
    }
}
