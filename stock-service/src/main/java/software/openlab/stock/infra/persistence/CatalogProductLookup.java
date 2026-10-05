package software.openlab.stock.infra.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import software.openlab.stock.domain.shared.ProductId;

@ApplicationScoped
public class CatalogProductLookup {

    public Optional<Long> findInternalId(ProductId productId) {
        return CatalogProductEntity.<CatalogProductEntity>find("publicId", productId.toString())
                .firstResultOptional()
                .map(e -> e.id);
    }
}
