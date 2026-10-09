package software.openlab.customer.infra.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import software.openlab.customer.domain.address.Address;
import software.openlab.customer.domain.address.AddressId;
import software.openlab.customer.domain.address.AddressRepository;
import software.openlab.customer.domain.customer.CustomerId;

@ApplicationScoped
public class AddressRepositoryImpl implements AddressRepository {

    @Override
    public List<Address> findByCustomer(CustomerId customerId) {
        return AddressEntity.<AddressEntity>list("customer.publicId = ?1 order by position, id", customerId.toString())
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Address> findById(CustomerId customerId, AddressId addressId) {
        return findEntity(customerId, addressId).map(this::toDomain);
    }

    @Override
    public long countByCustomer(CustomerId customerId) {
        return AddressEntity.count("customer.publicId", customerId.toString());
    }

    @Override
    public void insert(CustomerId customerId, Address address) {
        CustomerEntity customer = CustomerEntity.<CustomerEntity>find("publicId", customerId.toString())
                .firstResultOptional()
                .orElseThrow(() -> new IllegalStateException("customer not found: " + customerId));

        AddressEntity entity = new AddressEntity();
        entity.customer = customer;
        entity.publicId = address.addressId().toString();
        // Next position = number of addresses already stored (they are only appended or removed).
        entity.position = (int) countByCustomer(customerId);
        copyToEntity(address, entity);
        AddressEntity.persist(entity);
    }

    @Override
    public void update(CustomerId customerId, Address address) {
        AddressEntity entity = findEntity(customerId, address.addressId())
                .orElseThrow(() -> new IllegalStateException("address not found: " + address.addressId()));
        copyToEntity(address, entity);
    }

    @Override
    public void deleteById(CustomerId customerId, AddressId addressId) {
        findEntity(customerId, addressId).ifPresent(AddressEntity::delete);
    }

    @Override
    public void clearDefault(CustomerId customerId, AddressId exceptAddressId) {
        // Bulk update runs immediately, so it precedes the flush of the row that is becoming the default
        // (the partial unique index allows at most one default per customer).
        if (exceptAddressId == null) {
            AddressEntity.update("isDefault = false where isDefault = true and customer.publicId = ?1",
                    customerId.toString());
        } else {
            AddressEntity.update(
                    "isDefault = false where isDefault = true and customer.publicId = ?1 and publicId <> ?2",
                    customerId.toString(), exceptAddressId.toString());
        }
    }

    private Optional<AddressEntity> findEntity(CustomerId customerId, AddressId addressId) {
        return AddressEntity.<AddressEntity>find("publicId = ?1 and customer.publicId = ?2",
                        addressId.toString(), customerId.toString())
                .firstResultOptional();
    }

    private void copyToEntity(Address a, AddressEntity e) {
        e.label = a.label();
        e.zipCode = a.zipCode();
        e.street = a.street();
        e.streetNumber = a.number();
        e.complement = a.complement();
        e.neighborhood = a.neighborhood();
        e.city = a.city();
        e.state = a.state();
        e.isDefault = a.isDefault();
    }

    private Address toDomain(AddressEntity e) {
        return new Address(AddressId.of(e.publicId), e.label, e.zipCode, e.street, e.streetNumber, e.complement,
                e.neighborhood, e.city, e.state, e.isDefault);
    }
}
