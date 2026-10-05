package software.openlab.catalog.infra.persistence;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import software.openlab.catalog.domain.shared.PageResult;
import software.openlab.catalog.domain.supplier.Supplier;
import software.openlab.catalog.domain.supplier.SupplierId;
import software.openlab.catalog.domain.supplier.SupplierRepository;

@ApplicationScoped
public class SupplierRepositoryImpl implements SupplierRepository {

    @Override
    public Optional<Supplier> findById(SupplierId supplierId) {
        return SupplierEntity.<SupplierEntity>find("publicId", supplierId.toString())
                .firstResultOptional()
                .map(this::toDomain);
    }

    @Override
    public boolean existsByDocument(String document, SupplierId excludeSupplierId) {
        if (excludeSupplierId == null) {
            return SupplierEntity.count("document = ?1", document) > 0;
        }
        return SupplierEntity.count("document = ?1 and publicId <> ?2", document, excludeSupplierId.toString()) > 0;
    }

    @Override
    public PageResult<Supplier> find(String q, int page, int pageSize) {
        Sort sort = Sort.by("createdAt", Sort.Direction.Descending);

        var query = (q == null || q.isBlank())
                ? SupplierEntity.find("", sort)
                : SupplierEntity.find("lower(name) like lower(?1)", sort, "%" + q + "%");

        long total = query.count();
        List<Supplier> data = query.page(Page.of(page - 1, pageSize))
                .<SupplierEntity>list()
                .stream()
                .map(this::toDomain)
                .toList();

        return new PageResult<>(data, page, pageSize, total);
    }

    @Override
    public void insert(Supplier supplier) {
        SupplierEntity entity = new SupplierEntity();
        entity.publicId = supplier.getSupplierId().toString();
        entity.name = supplier.getName();
        entity.document = supplier.getDocument();
        entity.createdAt = supplier.getCreatedAt();
        SupplierEntity.persist(entity);
    }

    @Override
    public void update(Supplier supplier) {
        SupplierEntity entity = SupplierEntity.<SupplierEntity>find("publicId", supplier.getSupplierId().toString())
                .firstResultOptional()
                .orElseThrow(() -> new IllegalStateException("supplier not found: " + supplier.getSupplierId()));
        entity.name = supplier.getName();
        entity.document = supplier.getDocument();
        entity.updatedAt = Instant.now();
    }

    @Override
    public void deleteById(SupplierId supplierId) {
        SupplierEntity.delete("publicId", supplierId.toString());
    }

    @Override
    public boolean existsById(SupplierId supplierId) {
        return SupplierEntity.count("publicId", supplierId.toString()) > 0;
    }

    @Override
    public boolean hasProducts(SupplierId supplierId) {
        return ProductEntity.count("defaultSupplier.publicId = ?1", supplierId.toString()) > 0;
    }

    private Supplier toDomain(SupplierEntity e) {
        return new Supplier(SupplierId.of(e.publicId), e.name, e.document, e.createdAt, e.updatedAt);
    }
}
