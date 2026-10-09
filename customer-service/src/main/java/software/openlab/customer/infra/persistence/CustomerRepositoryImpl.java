package software.openlab.customer.infra.persistence;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import software.openlab.customer.domain.customer.Customer;
import software.openlab.customer.domain.customer.CustomerId;
import software.openlab.customer.domain.customer.CustomerRepository;
import software.openlab.customer.domain.customer.CustomerStatus;
import software.openlab.customer.domain.customer.CustomerType;
import software.openlab.customer.domain.shared.PageResult;

@ApplicationScoped
public class CustomerRepositoryImpl implements CustomerRepository {

    @Override
    public Optional<Customer> findById(CustomerId customerId) {
        return findEntity(customerId).map(this::toDomain);
    }

    @Override
    public boolean existsByDocument(String document, CustomerId excludeCustomerId) {
        if (excludeCustomerId == null) {
            return CustomerEntity.count("document = ?1", document) > 0;
        }
        return CustomerEntity.count("document = ?1 and publicId <> ?2", document, excludeCustomerId.toString()) > 0;
    }

    @Override
    public PageResult<Customer> find(String q, CustomerType type, CustomerStatus status, int page, int pageSize) {
        List<String> conditions = new ArrayList<>();
        Parameters params = new Parameters();

        if (q != null && !q.isBlank()) {
            String term = q.trim();
            String digits = term.replaceAll("\\D", "");
            String match = "lower(name) like :q or lower(tradeName) like :q or lower(email) like :q";
            params.and("q", "%" + term.toLowerCase() + "%");
            if (!digits.isEmpty()) {
                match += " or document like :digits";
                params.and("digits", "%" + digits + "%");
            }
            conditions.add("(" + match + ")");
        }
        if (type != null) {
            conditions.add("type = :type");
            params.and("type", type.name());
        }
        if (status != null) {
            conditions.add("status = :status");
            params.and("status", status.name());
        }

        Sort sort = Sort.by("createdAt", Sort.Direction.Descending);
        var query = conditions.isEmpty()
                ? CustomerEntity.find("", sort)
                : CustomerEntity.find(String.join(" and ", conditions), sort, params);

        long total = query.count();
        List<Customer> data = query.page(Page.of(page - 1, pageSize))
                .<CustomerEntity>list()
                .stream()
                .map(this::toDomain)
                .toList();

        return new PageResult<>(data, page, pageSize, total);
    }

    @Override
    public void insert(Customer customer) {
        CustomerEntity entity = new CustomerEntity();
        entity.publicId = customer.getCustomerId().toString();
        entity.type = customer.getType().name();
        entity.createdAt = customer.getCreatedAt();
        copyToEntity(customer, entity);
        CustomerEntity.persist(entity);
    }

    @Override
    public void update(Customer customer) {
        CustomerEntity entity = findEntity(customer.getCustomerId())
                .orElseThrow(() -> new IllegalStateException("customer not found: " + customer.getCustomerId()));
        copyToEntity(customer, entity);
        entity.updatedAt = Instant.now();
    }

    @Override
    public void deleteById(CustomerId customerId) {
        // customer_addresses rows go with it (ON DELETE CASCADE).
        CustomerEntity.delete("publicId", customerId.toString());
    }

    @Override
    public boolean existsById(CustomerId customerId) {
        return CustomerEntity.count("publicId", customerId.toString()) > 0;
    }

    private Optional<CustomerEntity> findEntity(CustomerId customerId) {
        return CustomerEntity.<CustomerEntity>find("publicId", customerId.toString()).firstResultOptional();
    }

    /** Everything except identity, type and creation time (immutable after insert). */
    private void copyToEntity(Customer c, CustomerEntity e) {
        e.status = c.getStatus().name();
        e.name = c.getName();
        e.tradeName = c.getTradeName();
        e.document = c.getDocument();
        e.stateRegistration = c.getStateRegistration();
        e.birthDate = c.getBirthDate();
        e.email = c.getEmail();
        e.phone = c.getPhone();
    }

    private Customer toDomain(CustomerEntity e) {
        return new Customer(
                CustomerId.of(e.publicId),
                CustomerType.valueOf(e.type),
                CustomerStatus.valueOf(e.status),
                e.name,
                e.tradeName,
                e.document,
                e.stateRegistration,
                e.birthDate,
                e.email,
                e.phone,
                e.createdAt,
                e.updatedAt);
    }
}
