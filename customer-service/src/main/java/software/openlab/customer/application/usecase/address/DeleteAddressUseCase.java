package software.openlab.customer.application.usecase.address;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.customer.application.usecase.customer.GetCustomerByIdUseCase;
import software.openlab.customer.domain.address.AddressEvents;
import software.openlab.customer.domain.address.AddressId;
import software.openlab.customer.domain.address.AddressRepository;
import software.openlab.customer.domain.customer.CustomerId;
import software.openlab.customer.domain.shared.DomainEvent;
import software.openlab.customer.domain.shared.NotFoundException;

@ApplicationScoped
public class DeleteAddressUseCase {

    @Inject
    AddressRepository repository;

    @Inject
    GetCustomerByIdUseCase getCustomerById;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public void execute(CustomerId customerId, AddressId addressId) {
        getCustomerById.execute(customerId); // 404 when the customer does not exist
        repository.findById(customerId, addressId)
                .orElseThrow(() -> new NotFoundException("error.address.notFound", addressId));

        repository.deleteById(customerId, addressId);

        events.fire(new DomainEvent(AddressEvents.DELETED,
                new AddressEvents.AddressDeletedPayload(addressId.toString(), customerId.toString())));
    }
}
