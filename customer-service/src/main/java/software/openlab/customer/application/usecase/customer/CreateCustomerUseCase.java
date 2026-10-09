package software.openlab.customer.application.usecase.customer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.customer.domain.customer.Customer;
import software.openlab.customer.domain.customer.CustomerCommand;
import software.openlab.customer.domain.customer.CustomerDetails;
import software.openlab.customer.domain.customer.CustomerEvents;
import software.openlab.customer.domain.customer.CustomerRepository;
import software.openlab.customer.domain.customer.CustomerType;
import software.openlab.customer.domain.shared.ConflictException;
import software.openlab.customer.domain.shared.DomainEvent;

@ApplicationScoped
public class CreateCustomerUseCase {

    @Inject
    CustomerRepository repository;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public Customer execute(String type, CustomerCommand command) {
        CustomerType customerType = CustomerType.parse(type);
        CustomerDetails details = command.toDetails(customerType);

        if (repository.existsByDocument(details.document(), null)) {
            throw new ConflictException("error.customer.duplicate");
        }

        Customer customer = Customer.newCustomer(customerType, details);
        repository.insert(customer);

        events.fire(new DomainEvent(CustomerEvents.CREATED, CustomerEvents.CustomerPayload.of(customer)));

        return customer;
    }
}
