package software.openlab.customer.application.usecase.address;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.customer.application.usecase.customer.GetCustomerByIdUseCase;
import software.openlab.customer.domain.address.Address;
import software.openlab.customer.domain.address.AddressEvents;
import software.openlab.customer.domain.address.AddressRepository;
import software.openlab.customer.domain.customer.CustomerId;
import software.openlab.customer.domain.shared.BadRequestException;
import software.openlab.customer.domain.shared.DomainEvent;

/** Adds an address to a customer; flagging it default clears the customer's previous default. */
@ApplicationScoped
public class AddAddressUseCase {

    @Inject
    AddressRepository repository;

    @Inject
    GetCustomerByIdUseCase getCustomerById;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public Address execute(CustomerId customerId, Address raw) {
        getCustomerById.execute(customerId); // 404 when the customer does not exist
        Address address = Address.validated(null, raw);

        if (repository.countByCustomer(customerId) >= Address.MAX_PER_CUSTOMER) {
            throw new BadRequestException("error.address.tooMany", Address.MAX_PER_CUSTOMER);
        }
        if (address.isDefault()) {
            repository.clearDefault(customerId, null);
        }
        repository.insert(customerId, address);

        events.fire(new DomainEvent(AddressEvents.CREATED,
                AddressEvents.AddressPayload.of(customerId.toString(), address)));
        return address;
    }
}
