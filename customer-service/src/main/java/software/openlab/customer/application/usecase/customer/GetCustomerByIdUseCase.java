package software.openlab.customer.application.usecase.customer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.openlab.customer.domain.customer.Customer;
import software.openlab.customer.domain.customer.CustomerId;
import software.openlab.customer.domain.customer.CustomerRepository;
import software.openlab.customer.domain.shared.NotFoundException;

@ApplicationScoped
public class GetCustomerByIdUseCase {

    @Inject
    CustomerRepository repository;

    public Customer execute(CustomerId customerId) {
        return repository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("error.customer.notFound", customerId));
    }
}
