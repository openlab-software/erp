package software.openlab.catalog.infra.persistence;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import software.openlab.catalog.domain.product.BarcodeType;
import software.openlab.catalog.domain.product.ProductBarcode;
import software.openlab.catalog.domain.product.ProductBarcodeId;
import software.openlab.catalog.domain.product.ProductBarcodeRepository;
import software.openlab.catalog.domain.product.ProductId;
import software.openlab.catalog.domain.shared.NotFoundException;

@ApplicationScoped
public class ProductBarcodeRepositoryImpl implements ProductBarcodeRepository {

    @Override
    public Optional<ProductBarcode> findById(ProductBarcodeId barcodeId) {
        return ProductBarcodeEntity.<ProductBarcodeEntity>find("publicId", barcodeId.toString())
                .firstResultOptional()
                .map(this::toDomain);
    }

    @Override
    public List<ProductBarcode> findByProductId(ProductId productId) {
        return ProductBarcodeEntity.<ProductBarcodeEntity>find("product.publicId", Sort.by("createdAt"), productId.toString())
                .list()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean existsByCode(String code) {
        return ProductBarcodeEntity.count("code", code) > 0;
    }

    @Override
    public void insert(ProductBarcode barcode) {
        ProductEntity product = ProductEntity.<ProductEntity>find("publicId", barcode.getProductId().toString())
                .firstResultOptional()
                .orElseThrow(() -> new NotFoundException("error.product.notFound", barcode.getProductId()));

        ProductBarcodeEntity entity = new ProductBarcodeEntity();
        entity.publicId = barcode.getBarcodeId().toString();
        entity.product = product;
        entity.code = barcode.getCode();
        entity.type = barcode.getType().name();
        entity.createdAt = barcode.getCreatedAt();
        ProductBarcodeEntity.persist(entity);
    }

    @Override
    public void deleteById(ProductBarcodeId barcodeId) {
        ProductBarcodeEntity.delete("publicId", barcodeId.toString());
    }

    private ProductBarcode toDomain(ProductBarcodeEntity e) {
        return new ProductBarcode(
                ProductBarcodeId.of(e.publicId),
                ProductId.of(e.product.publicId),
                e.code,
                BarcodeType.valueOf(e.type),
                e.createdAt
        );
    }
}
