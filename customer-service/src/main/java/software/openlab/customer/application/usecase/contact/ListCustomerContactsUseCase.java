package software.openlab.customer.application.usecase.contact;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import software.openlab.customer.application.usecase.customer.GetCustomerByIdUseCase;
import software.openlab.customer.domain.contact.Contact;
import software.openlab.customer.domain.contact.ContactRepository;
import software.openlab.customer.domain.customer.CustomerId;

@ApplicationScoped
public class ListCustomerContactsUseCase {

    @Inject
    ContactRepository repository;

    @Inject
    GetCustomerByIdUseCase getCustomerById;

    public List<Contact> execute(CustomerId customerId) {
        getCustomerById.execute(customerId); // 404 when the customer does not exist
        return repository.findByCustomer(customerId);
    }
}
