package software.openlab.customer.application.usecase.customer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.customer.domain.customer.CustomerEvents;
import software.openlab.customer.domain.customer.CustomerId;
import software.openlab.customer.domain.customer.CustomerRepository;
import software.openlab.customer.domain.shared.DomainEvent;
import software.openlab.customer.domain.shared.NotFoundException;

@ApplicationScoped
public class DeleteCustomerUseCase {

    @Inject
    CustomerRepository repository;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public void execute(CustomerId customerId) {
        if (!repository.existsById(customerId)) {
            throw new NotFoundException("error.customer.notFound", customerId);
        }

        repository.deleteById(customerId);

        events.fire(new DomainEvent(CustomerEvents.DELETED, new CustomerEvents.CustomerDeletedPayload(customerId.value())));
    }
}
