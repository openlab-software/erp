package software.openlab.customer.domain.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import software.openlab.customer.domain.shared.BadRequestException;

class AddressTest {

    private static Address raw(String label, String zip, String state, boolean isDefault) {
        return new Address(null, label, zip, " Av. Paulista ", "1000", " ", null, "São Paulo", state, isDefault);
    }

    @Test
    void trimsDropsBlanksUppercasesUfAndKeepsOnlyZipDigits() {
        var address = Address.validated(null, raw(" Casa ", "01310-100", "sp", true));

        assertNotNull(address.addressId());
        assertEquals("Casa", address.label());
        assertEquals("01310100", address.zipCode());
        assertEquals("Av. Paulista", address.street());
        assertNull(address.complement());
        assertEquals("SP", address.state());
        assertTrue(address.isDefault());
    }

    @Test
    void keepsTheGivenIdWhenEditingAndGeneratesOneWhenCreating() {
        var id = AddressId.generate();
        assertEquals(id, Address.validated(id, raw("A", "01310100", "SP", false)).addressId());
        assertNotNull(Address.validated(null, raw("A", "01310100", "SP", false)).addressId());
    }

    @Test
    void rejectsBadZipBadStateAndEntriesWithNoAddressData() {
        assertThrows(BadRequestException.class, () -> Address.validated(null, raw("A", "123", "SP", false)));
        assertThrows(BadRequestException.class, () -> Address.validated(null, raw("A", "01310100", "XX", false)));
        // only label / default flag informed: not an address
        assertThrows(BadRequestException.class, () -> Address.validated(null,
                new Address(null, "Só rótulo", " ", null, "", null, null, null, null, true)));
    }

    @Test
    void addressIdsMustFollowTheAddressUlidFormat() {
        assertThrows(BadRequestException.class, () -> AddressId.of("customer_01J6Z3K9QZXJ6R6JYX8G0XJ0KQ"));
        assertThrows(BadRequestException.class, () -> AddressId.of("address_nope"));
        assertFalse(AddressId.generate().value().isBlank());
    }
}
