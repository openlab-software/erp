package software.openlab.customer.domain.customer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import software.openlab.customer.domain.shared.BadRequestException;

class CustomerDetailsTest {

    private static final String CPF = "529.982.247-25";
    private static final String CNPJ = "11.222.333/0001-81";

    @Test
    void normalizesValidCpfAndCnpjToDigits() {
        assertEquals("52998224725", Document.normalize(CustomerType.INDIVIDUAL, CPF));
        assertEquals("11222333000181", Document.normalize(CustomerType.COMPANY, CNPJ));
    }

    @Test
    void rejectsBadCheckDigitsRepeatedDigitsAndWrongKind() {
        assertThrows(BadRequestException.class, () -> Document.normalize(CustomerType.INDIVIDUAL, "529.982.247-26"));
        assertThrows(BadRequestException.class, () -> Document.normalize(CustomerType.INDIVIDUAL, "111.111.111-11"));
        assertThrows(BadRequestException.class, () -> Document.normalize(CustomerType.COMPANY, "11.222.333/0001-82"));
        // a valid CPF is not a valid document for a company, and vice versa
        assertThrows(BadRequestException.class, () -> Document.normalize(CustomerType.COMPANY, CPF));
        assertThrows(BadRequestException.class, () -> Document.normalize(CustomerType.INDIVIDUAL, CNPJ));
    }

    @Test
    void individualDropsCompanyOnlyFields() {
        var details = CustomerDetails.validated(CustomerType.INDIVIDUAL, " Maria ", "Fantasia", CPF, "123",
                LocalDate.of(1990, 5, 1), "maria@example.com", " 11 99999-0000 ");

        assertEquals("Maria", details.name());
        assertNull(details.tradeName());
        assertNull(details.stateRegistration());
        assertEquals(LocalDate.of(1990, 5, 1), details.birthDate());
        assertEquals("11 99999-0000", details.phone());
    }

    @Test
    void companyDropsBirthDateAndKeepsTradeName() {
        var details = CustomerDetails.validated(CustomerType.COMPANY, "ACME Ltda", "ACME", CNPJ, "ISENTO",
                LocalDate.of(1990, 5, 1), null, null);

        assertNull(details.birthDate());
        assertEquals("ACME", details.tradeName());
        assertEquals("ISENTO", details.stateRegistration());
        assertEquals("11222333000181", details.document());
    }

    @Test
    void rejectsBlankNameBadEmailAndFutureBirthDate() {
        assertThrows(BadRequestException.class, () -> CustomerDetails.validated(
                CustomerType.INDIVIDUAL, " ", null, CPF, null, null, null, null));
        assertThrows(BadRequestException.class, () -> CustomerDetails.validated(
                CustomerType.INDIVIDUAL, "Maria", null, CPF, null, null, "not-an-email", null));
        assertThrows(BadRequestException.class, () -> CustomerDetails.validated(
                CustomerType.INDIVIDUAL, "Maria", null, CPF, null, LocalDate.now().plusDays(1), null, null));
    }

    @Test
    void applyReplacesEveryEditableField() {
        var customer = Customer.newCustomer(CustomerType.COMPANY, CustomerDetails.validated(
                CustomerType.COMPANY, "ACME", "A", CNPJ, null, null, null, null));
        customer.apply(CustomerDetails.validated(
                CustomerType.COMPANY, "ACME 2", null, CNPJ, "ISENTO", null, "a@b.co", null));

        assertEquals("ACME 2", customer.getName());
        assertNull(customer.getTradeName());
        assertEquals("ISENTO", customer.getStateRegistration());
        assertEquals(CustomerStatus.ACTIVE, customer.getStatus());
    }
}
