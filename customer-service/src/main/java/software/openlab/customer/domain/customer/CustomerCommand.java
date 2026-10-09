package software.openlab.customer.domain.customer;

import java.time.LocalDate;

/**
 * Raw (unvalidated) editable data coming from the REST layer. {@link #toDetails} applies
 * the validation/normalization rules for the customer's {@code type}.
 */
public record CustomerCommand(
        String name,
        String tradeName,
        String document,
        String stateRegistration,
        LocalDate birthDate,
        String email,
        String phone) {

    public CustomerDetails toDetails(CustomerType type) {
        return CustomerDetails.validated(type, name, tradeName, document, stateRegistration, birthDate, email, phone);
    }
}
