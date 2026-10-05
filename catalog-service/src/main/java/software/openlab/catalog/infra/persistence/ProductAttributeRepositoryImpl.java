package software.openlab.catalog.infra.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import software.openlab.catalog.domain.product.ProductAttribute;
import software.openlab.catalog.domain.product.ProductAttributeRepository;
import software.openlab.catalog.domain.product.ProductId;
import software.openlab.catalog.domain.shared.NotFoundException;

@ApplicationScoped
public class ProductAttributeRepositoryImpl implements ProductAttributeRepository {

    @Override
    public List<ProductAttribute> findByProductId(ProductId productId) {
        return ProductAttributeEntity.<ProductAttributeEntity>find("product.publicId", productId.toString())
                .list()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void replaceAll(ProductId productId, List<ProductAttribute> attributes) {
        ProductEntity product = ProductEntity.<ProductEntity>find("publicId", productId.toString())
                .firstResultOptional()
                .orElseThrow(() -> new NotFoundException("error.product.notFound", productId));

        ProductAttributeEntity.delete("product.publicId", productId.toString());

        for (ProductAttribute attribute : attributes) {
            ProductAttributeEntity entity = new ProductAttributeEntity();
            entity.product = product;
            entity.name = attribute.name();
            entity.value = attribute.value();
            ProductAttributeEntity.persist(entity);
        }
    }

    private ProductAttribute toDomain(ProductAttributeEntity e) {
        return new ProductAttribute(e.name, e.value);
    }
}
