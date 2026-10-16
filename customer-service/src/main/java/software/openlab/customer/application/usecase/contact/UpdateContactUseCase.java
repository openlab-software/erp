package software.openlab.customer.application.usecase.contact;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.customer.application.usecase.customer.GetCustomerByIdUseCase;
import software.openlab.customer.domain.contact.Contact;
import software.openlab.customer.domain.contact.ContactEvents;
import software.openlab.customer.domain.contact.ContactId;
import software.openlab.customer.domain.contact.ContactRepository;
import software.openlab.customer.domain.customer.CustomerId;
import software.openlab.customer.domain.shared.DomainEvent;
import software.openlab.customer.domain.shared.NotFoundException;

/** PUT semantics for one contact; flagging it primary clears the previous primary, unflagging leaves none. */
@ApplicationScoped
public class UpdateContactUseCase {

    @Inject
    ContactRepository repository;

    @Inject
    GetCustomerByIdUseCase getCustomerById;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public Contact execute(CustomerId customerId, ContactId contactId, Contact raw) {
        getCustomerById.execute(customerId); // 404 when the customer does not exist
        repository.findById(customerId, contactId)
                .orElseThrow(() -> new NotFoundException("error.contact.notFound", contactId));
        Contact contact = Contact.validated(contactId, raw);
        if (contact.isPrimary()) {
            repository.clearPrimary(customerId, contactId);
        }
        repository.update(customerId, contact);
        events.fire(new DomainEvent(ContactEvents.UPDATED,
                ContactEvents.ContactPayload.of(customerId.toString(), contact)));
        return contact;
    }
}
