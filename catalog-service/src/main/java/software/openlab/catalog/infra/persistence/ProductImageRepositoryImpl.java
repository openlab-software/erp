package software.openlab.catalog.infra.persistence;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import software.openlab.catalog.domain.product.ProductId;
import software.openlab.catalog.domain.product.ProductImage;
import software.openlab.catalog.domain.product.ProductImageId;
import software.openlab.catalog.domain.product.ProductImageRepository;
import software.openlab.catalog.domain.shared.NotFoundException;

@ApplicationScoped
public class ProductImageRepositoryImpl implements ProductImageRepository {

    @Override
    public Optional<ProductImage> findById(ProductImageId imageId) {
        return ProductImageEntity.<ProductImageEntity>find("publicId", imageId.toString())
                .firstResultOptional()
                .map(this::toDomain);
    }

    @Override
    public List<ProductImage> findByProductId(ProductId productId) {
        return ProductImageEntity.<ProductImageEntity>find("product.publicId", Sort.by("createdAt"), productId.toString())
                .list()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long countByProductId(ProductId productId) {
        return ProductImageEntity.count("product.publicId", productId.toString());
    }

    @Override
    public void insert(ProductImage image) {
        ProductEntity product = ProductEntity.<ProductEntity>find("publicId", image.getProductId().toString())
                .firstResultOptional()
                .orElseThrow(() -> new NotFoundException("error.product.notFound", image.getProductId()));

        ProductImageEntity entity = new ProductImageEntity();
        entity.publicId = image.getImageId().toString();
        entity.product = product;
        entity.url = image.getUrl();
        entity.primary = image.isPrimary();
        entity.createdAt = image.getCreatedAt();
        ProductImageEntity.persist(entity);
    }

    @Override
    public void deleteById(ProductImageId imageId) {
        ProductImageEntity.delete("publicId", imageId.toString());
    }

    @Override
    public void clearPrimaryForProduct(ProductId productId) {
        ProductImageEntity.update("primary = false where product.publicId = ?1", productId.toString());
    }

    @Override
    public void setPrimary(ProductImageId imageId) {
        ProductImageEntity.update("primary = true where publicId = ?1", imageId.toString());
    }

    private ProductImage toDomain(ProductImageEntity e) {
        return new ProductImage(
                ProductImageId.of(e.publicId),
                ProductId.of(e.product.publicId),
                e.url,
                e.primary,
                e.createdAt
        );
    }
}
