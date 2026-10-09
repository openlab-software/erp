package software.openlab.customer.application.usecase.customer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.openlab.customer.domain.customer.Customer;
import software.openlab.customer.domain.customer.CustomerRepository;
import software.openlab.customer.domain.customer.CustomerStatus;
import software.openlab.customer.domain.customer.CustomerType;
import software.openlab.customer.domain.shared.PageResult;

@ApplicationScoped
public class ListCustomersUseCase {

    @Inject
    CustomerRepository repository;

    public PageResult<Customer> execute(String q, String type, String status, Integer page, Integer pageSize) {
        CustomerType typeFilter = isBlank(type) ? null : CustomerType.parse(type);
        CustomerStatus statusFilter = isBlank(status) ? null : CustomerStatus.parse(status);
        return repository.find(q, typeFilter, statusFilter, PageResult.normalizePage(page),
                PageResult.normalizePageSize(pageSize));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
