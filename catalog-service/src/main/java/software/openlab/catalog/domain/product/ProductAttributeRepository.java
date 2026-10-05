package software.openlab.catalog.domain.product;

import java.util.List;

public interface ProductAttributeRepository {

    List<ProductAttribute> findByProductId(ProductId productId);

    void replaceAll(ProductId productId, List<ProductAttribute> attributes);
}
