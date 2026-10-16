package software.openlab.customer.domain.contact;

import java.util.List;
import java.util.Optional;
import software.openlab.customer.domain.customer.CustomerId;

public interface ContactRepository {

    /** All contacts of a customer: the primary first, then in the order they were created. */
    List<Contact> findByCustomer(CustomerId customerId);

    Optional<Contact> findById(CustomerId customerId, ContactId contactId);

    long countByCustomer(CustomerId customerId);

    void insert(CustomerId customerId, Contact contact);

    void update(CustomerId customerId, Contact contact);

    void deleteById(CustomerId customerId, ContactId contactId);

    /** Clears the primary flag of every contact of the customer except {@code exceptContactId} (may be null). */
    void clearPrimary(CustomerId customerId, ContactId exceptContactId);
}
