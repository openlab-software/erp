package software.openlab.customer.application.usecase.address;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.customer.application.usecase.customer.GetCustomerByIdUseCase;
import software.openlab.customer.domain.address.Address;
import software.openlab.customer.domain.address.AddressEvents;
import software.openlab.customer.domain.address.AddressId;
import software.openlab.customer.domain.address.AddressRepository;
import software.openlab.customer.domain.customer.CustomerId;
import software.openlab.customer.domain.shared.DomainEvent;
import software.openlab.customer.domain.shared.NotFoundException;

/** PUT semantics for one address; flagging it default clears the previous default, unflagging leaves none. */
@ApplicationScoped
public class UpdateAddressUseCase {

    @Inject
    AddressRepository repository;

    @Inject
    GetCustomerByIdUseCase getCustomerById;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public Address execute(CustomerId customerId, AddressId addressId, Address raw) {
        getCustomerById.execute(customerId); // 404 when the customer does not exist
        repository.findById(customerId, addressId)
                .orElseThrow(() -> new NotFoundException("error.address.notFound", addressId));
        Address address = Address.validated(addressId, raw);

        if (address.isDefault()) {
            repository.clearDefault(customerId, addressId);
        }
        repository.update(customerId, address);

        events.fire(new DomainEvent(AddressEvents.UPDATED,
                AddressEvents.AddressPayload.of(customerId.toString(), address)));
        return address;
    }
}
