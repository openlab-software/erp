package software.openlab.catalog.domain.product;

import java.util.List;
import java.util.Optional;
import software.openlab.catalog.domain.shared.PageResult;

public interface ProductRepository {

    Optional<Product> findById(ProductId productId);

    PageResult<Product> find(ProductFilter filter);

    /** Same filtering as {@link #find}, but returns every matching row (no pagination) — used by CSV export. */
    List<Product> findAll(ProductFilter filter);

    /**
     * Products with {@code status <> INACTIVE} whose description contains {@code q}
     * (case-insensitive), prefix matches ranked first, limited to {@code limit} rows.
     */
    List<Product> autocomplete(String q, int limit);

    void insert(Product product);

    void update(Product product);

    void deleteById(ProductId productId);

    boolean existsById(ProductId productId);
}
