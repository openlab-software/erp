package software.openlab.customer.application.usecase.customer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.customer.domain.customer.Customer;
import software.openlab.customer.domain.customer.CustomerCommand;
import software.openlab.customer.domain.customer.CustomerDetails;
import software.openlab.customer.domain.customer.CustomerEvents;
import software.openlab.customer.domain.customer.CustomerId;
import software.openlab.customer.domain.customer.CustomerRepository;
import software.openlab.customer.domain.shared.ConflictException;
import software.openlab.customer.domain.shared.DomainEvent;

/** PUT semantics: replaces every editable field. The customer type is immutable and always comes from the stored customer. */
@ApplicationScoped
public class UpdateCustomerUseCase {

    @Inject
    CustomerRepository repository;

    @Inject
    GetCustomerByIdUseCase getCustomerById;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public Customer execute(CustomerId customerId, CustomerCommand command) {
        Customer customer = getCustomerById.execute(customerId);
        CustomerDetails details = command.toDetails(customer.getType());

        if (repository.existsByDocument(details.document(), customerId)) {
            throw new ConflictException("error.customer.duplicate");
        }

        customer.apply(details);
        repository.update(customer);

        Customer updated = getCustomerById.execute(customerId);
        events.fire(new DomainEvent(CustomerEvents.UPDATED, CustomerEvents.CustomerPayload.of(updated)));

        return updated;
    }
}
