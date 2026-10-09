package software.openlab.customer.application.usecase.customer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.customer.domain.customer.Customer;
import software.openlab.customer.domain.customer.CustomerEvents;
import software.openlab.customer.domain.customer.CustomerId;
import software.openlab.customer.domain.customer.CustomerRepository;
import software.openlab.customer.domain.customer.CustomerStatus;
import software.openlab.customer.domain.shared.ConflictException;
import software.openlab.customer.domain.shared.DomainEvent;

@ApplicationScoped
public class ChangeCustomerStatusUseCase {

    @Inject
    CustomerRepository repository;

    @Inject
    GetCustomerByIdUseCase getCustomerById;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public Customer execute(CustomerId customerId, String status) {
        CustomerStatus newStatus = CustomerStatus.parse(status);

        Customer customer = getCustomerById.execute(customerId);
        if (customer.getStatus() == newStatus) {
            throw new ConflictException("error.customer.status.unchanged", newStatus);
        }

        customer.setStatus(newStatus);
        repository.update(customer);

        Customer updated = getCustomerById.execute(customerId);
        events.fire(new DomainEvent(CustomerEvents.UPDATED, CustomerEvents.CustomerPayload.of(updated)));

        return updated;
    }
}
