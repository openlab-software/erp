package software.openlab.customer.application.usecase.address;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import software.openlab.customer.application.usecase.customer.GetCustomerByIdUseCase;
import software.openlab.customer.domain.address.Address;
import software.openlab.customer.domain.address.AddressRepository;
import software.openlab.customer.domain.customer.CustomerId;

@ApplicationScoped
public class ListCustomerAddressesUseCase {

    @Inject
    AddressRepository repository;

    @Inject
    GetCustomerByIdUseCase getCustomerById;

    public List<Address> execute(CustomerId customerId) {
        getCustomerById.execute(customerId); // 404 when the customer does not exist
        return repository.findByCustomer(customerId);
    }
}
