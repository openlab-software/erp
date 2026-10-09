package software.openlab.customer.domain.address;

import java.util.List;
import java.util.Optional;
import software.openlab.customer.domain.customer.CustomerId;

public interface AddressRepository {

    /** All addresses of a customer, in the order they were created. */
    List<Address> findByCustomer(CustomerId customerId);

    Optional<Address> findById(CustomerId customerId, AddressId addressId);

    long countByCustomer(CustomerId customerId);

    void insert(CustomerId customerId, Address address);

    void update(CustomerId customerId, Address address);

    void deleteById(CustomerId customerId, AddressId addressId);

    /** Clears the default flag of every address of the customer except {@code exceptAddressId} (may be {@code null}). */
    void clearDefault(CustomerId customerId, AddressId exceptAddressId);
}
