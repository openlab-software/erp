package software.openlab.customer.application.usecase.contact;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.customer.application.usecase.customer.GetCustomerByIdUseCase;
import software.openlab.customer.domain.contact.ContactEvents;
import software.openlab.customer.domain.contact.ContactId;
import software.openlab.customer.domain.contact.ContactRepository;
import software.openlab.customer.domain.customer.CustomerId;
import software.openlab.customer.domain.shared.DomainEvent;
import software.openlab.customer.domain.shared.NotFoundException;

@ApplicationScoped
public class DeleteContactUseCase {

    @Inject
    ContactRepository repository;

    @Inject
    GetCustomerByIdUseCase getCustomerById;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public void execute(CustomerId customerId, ContactId contactId) {
        getCustomerById.execute(customerId); // 404 when the customer does not exist
        repository.findById(customerId, contactId)
                .orElseThrow(() -> new NotFoundException("error.contact.notFound", contactId));
        repository.deleteById(customerId, contactId);
        events.fire(new DomainEvent(ContactEvents.DELETED,
                new ContactEvents.ContactDeletedPayload(contactId.toString(), customerId.toString())));
    }
}
