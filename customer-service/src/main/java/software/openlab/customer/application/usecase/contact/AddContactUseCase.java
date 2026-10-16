package software.openlab.customer.application.usecase.contact;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.customer.application.usecase.customer.GetCustomerByIdUseCase;
import software.openlab.customer.domain.contact.Contact;
import software.openlab.customer.domain.contact.ContactEvents;
import software.openlab.customer.domain.contact.ContactRepository;
import software.openlab.customer.domain.customer.CustomerId;
import software.openlab.customer.domain.shared.BadRequestException;
import software.openlab.customer.domain.shared.DomainEvent;

/** Adds a contact person to a customer; flagging it primary clears the customer's previous primary contact. */
@ApplicationScoped
public class AddContactUseCase {

    @Inject
    ContactRepository repository;

    @Inject
    GetCustomerByIdUseCase getCustomerById;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public Contact execute(CustomerId customerId, Contact raw) {
        getCustomerById.execute(customerId); // 404 when the customer does not exist
        Contact contact = Contact.validated(null, raw);

        if (repository.countByCustomer(customerId) >= Contact.MAX_PER_CUSTOMER) {
            throw new BadRequestException("error.contact.tooMany", Contact.MAX_PER_CUSTOMER);
        }
        if (contact.isPrimary()) {
            repository.clearPrimary(customerId, null);
        }
        repository.insert(customerId, contact);

        events.fire(new DomainEvent(ContactEvents.CREATED,
                ContactEvents.ContactPayload.of(customerId.toString(), contact)));
        return contact;
    }
}
