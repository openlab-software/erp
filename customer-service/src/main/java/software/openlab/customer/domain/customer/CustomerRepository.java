package software.openlab.customer.domain.customer;

import java.util.Optional;
import software.openlab.customer.domain.shared.PageResult;

public interface CustomerRepository {

    Optional<Customer> findById(CustomerId customerId);

    boolean existsByDocument(String document, CustomerId excludeCustomerId);

    /** {@code q} matches name, trade name, document (digits) or email; the other filters may be {@code null}. */
    PageResult<Customer> find(String q, CustomerType type, CustomerStatus status, int page, int pageSize);

    void insert(Customer customer);

    void update(Customer customer);

    void deleteById(CustomerId customerId);

    boolean existsById(CustomerId customerId);
}
