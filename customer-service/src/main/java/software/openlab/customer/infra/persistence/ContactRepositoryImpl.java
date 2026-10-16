package software.openlab.customer.infra.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import software.openlab.customer.domain.contact.Contact;
import software.openlab.customer.domain.contact.ContactId;
import software.openlab.customer.domain.contact.ContactRepository;
import software.openlab.customer.domain.customer.CustomerId;

@ApplicationScoped
public class ContactRepositoryImpl implements ContactRepository {

    @Override
    public List<Contact> findByCustomer(CustomerId customerId) {
        return ContactEntity.<ContactEntity>list("customer.publicId = ?1 order by isPrimary desc, position, id",
                        customerId.toString())
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Contact> findById(CustomerId customerId, ContactId contactId) {
        return findEntity(customerId, contactId).map(this::toDomain);
    }

    @Override
    public long countByCustomer(CustomerId customerId) {
        return ContactEntity.count("customer.publicId", customerId.toString());
    }

    @Override
    public void insert(CustomerId customerId, Contact contact) {
        CustomerEntity customer = CustomerEntity.<CustomerEntity>find("publicId", customerId.toString())
                .firstResultOptional()
                .orElseThrow(() -> new IllegalStateException("customer not found: " + customerId));

        ContactEntity entity = new ContactEntity();
        entity.customer = customer;
        entity.publicId = contact.contactId().toString();
        // Next position = number of contacts already stored (they are only appended or removed).
        entity.position = (int) countByCustomer(customerId);
        copyToEntity(contact, entity);
        ContactEntity.persist(entity);
    }

    @Override
    public void update(CustomerId customerId, Contact contact) {
        ContactEntity entity = findEntity(customerId, contact.contactId())
                .orElseThrow(() -> new IllegalStateException("contact not found: " + contact.contactId()));
        copyToEntity(contact, entity);
    }

    @Override
    public void deleteById(CustomerId customerId, ContactId contactId) {
        findEntity(customerId, contactId).ifPresent(ContactEntity::delete);
    }

    @Override
    public void clearPrimary(CustomerId customerId, ContactId exceptContactId) {
        // Bulk update runs immediately, so it precedes the flush of the row that is becoming the primary
        // (the partial unique index allows at most one primary contact per customer).
        if (exceptContactId == null) {
            ContactEntity.update("isPrimary = false where isPrimary = true and customer.publicId = ?1",
                    customerId.toString());
        } else {
            ContactEntity.update(
                    "isPrimary = false where isPrimary = true and customer.publicId = ?1 and publicId <> ?2",
                    customerId.toString(), exceptContactId.toString());
        }
    }

    private Optional<ContactEntity> findEntity(CustomerId customerId, ContactId contactId) {
        return ContactEntity.<ContactEntity>find("publicId = ?1 and customer.publicId = ?2",
                        contactId.toString(), customerId.toString())
                .firstResultOptional();
    }

    private void copyToEntity(Contact c, ContactEntity e) {
        e.name = c.name();
        e.jobTitle = c.jobTitle();
        e.email = c.email();
        e.phone = c.phone();
        e.notes = c.notes();
        e.isPrimary = c.isPrimary();
    }

    private Contact toDomain(ContactEntity e) {
        return new Contact(ContactId.of(e.publicId), e.name, e.jobTitle, e.email, e.phone, e.notes, e.isPrimary);
    }
}
