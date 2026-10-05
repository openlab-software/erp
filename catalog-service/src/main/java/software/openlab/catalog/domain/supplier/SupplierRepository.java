package software.openlab.catalog.domain.supplier;

import java.util.Optional;
import software.openlab.catalog.domain.shared.PageResult;

public interface SupplierRepository {

    Optional<Supplier> findById(SupplierId supplierId);

    boolean existsByDocument(String document, SupplierId excludeSupplierId);

    PageResult<Supplier> find(String q, int page, int pageSize);

    void insert(Supplier supplier);

    void update(Supplier supplier);

    void deleteById(SupplierId supplierId);

    boolean existsById(SupplierId supplierId);

    boolean hasProducts(SupplierId supplierId);
}
