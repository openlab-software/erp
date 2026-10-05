package software.openlab.catalog.domain.product;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository {

    Optional<ProductImage> findById(ProductImageId imageId);

    /** Ordered by {@code createdAt} ascending (oldest first). */
    List<ProductImage> findByProductId(ProductId productId);

    long countByProductId(ProductId productId);

    void insert(ProductImage image);

    void deleteById(ProductImageId imageId);

    void clearPrimaryForProduct(ProductId productId);

    void setPrimary(ProductImageId imageId);
}
