package software.openlab.customer.domain.customer;

import java.time.Instant;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** A customer's personal/company data. Addresses live in their own module ({@code domain.address}). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    private CustomerId customerId;
    private CustomerType type;
    private CustomerStatus status;
    private String name;
    private String tradeName;
    /** CPF or CNPJ, digits only. */
    private String document;
    private String stateRegistration;
    private LocalDate birthDate;
    private String email;
    private String phone;
    private Instant createdAt;
    private Instant updatedAt;

    public static Customer newCustomer(CustomerType type, CustomerDetails details) {
        Customer customer = new Customer(CustomerId.generate(), type, CustomerStatus.ACTIVE,
                null, null, null, null, null, null, null, Instant.now(), null);
        customer.apply(details);
        return customer;
    }

    /** Replaces every editable field with {@code details} (PUT semantics). */
    public void apply(CustomerDetails details) {
        this.name = details.name();
        this.tradeName = details.tradeName();
        this.document = details.document();
        this.stateRegistration = details.stateRegistration();
        this.birthDate = details.birthDate();
        this.email = details.email();
        this.phone = details.phone();
    }
}
