package software.openlab.customer.domain.address;

import java.util.Set;
import software.openlab.customer.domain.shared.BadRequestException;

/**
 * One postal address of a customer. Every part is nullable; {@code zipCode} is digits only,
 * {@code state} an uppercase UF, {@code label} a free nickname ("Casa", "Matriz", "Entrega").
 * A customer has up to {@link #MAX_PER_CUSTOMER} addresses and at most one of them is the default.
 *
 * <p>This module only knows the customer by its id (see {@link AddressRepository}); whether the customer
 * exists is checked by the use cases.
 */
public record Address(
        AddressId addressId,
        String label,
        String zipCode,
        String street,
        String number,
        String complement,
        String neighborhood,
        String city,
        String state,
        boolean isDefault) {

    public static final int MAX_PER_CUSTOMER = 10;

    private static final Set<String> UFS = Set.of(
            "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA",
            "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO");

    /**
     * Trims, drops blanks, validates zip/UF and checks that some address data was informed.
     * The result keeps {@code id} (the address being edited) or gets a new one when {@code id} is {@code null}.
     */
    public static Address validated(AddressId id, Address raw) {
        String zip = blankToNull(raw.zipCode() == null ? null : raw.zipCode().replaceAll("\\D", ""));
        if (zip != null && zip.length() != 8) {
            throw new BadRequestException("error.field.invalid", "zip_code", raw.zipCode());
        }
        String uf = blankToNull(raw.state() == null ? null : raw.state().trim().toUpperCase());
        if (uf != null && !UFS.contains(uf)) {
            throw new BadRequestException("error.address.state.invalid", raw.state());
        }
        Address address = new Address(id != null ? id : AddressId.generate(), blankToNull(raw.label()), zip,
                blankToNull(raw.street()), blankToNull(raw.number()), blankToNull(raw.complement()),
                blankToNull(raw.neighborhood()), blankToNull(raw.city()), uf, raw.isDefault());
        if (address.isEmpty()) {
            throw new BadRequestException("error.address.empty");
        }
        return address;
    }

    /** Ignores {@code label} and {@code isDefault}: an entry with only those set carries no address. */
    public boolean isEmpty() {
        return zipCode == null && street == null && number == null && complement == null
                && neighborhood == null && city == null && state == null;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
