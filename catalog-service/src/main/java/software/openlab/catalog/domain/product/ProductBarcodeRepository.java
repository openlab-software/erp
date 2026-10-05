package software.openlab.catalog.domain.product;

import java.util.List;
import java.util.Optional;

public interface ProductBarcodeRepository {

    Optional<ProductBarcode> findById(ProductBarcodeId barcodeId);

    List<ProductBarcode> findByProductId(ProductId productId);

    boolean existsByCode(String code);

    void insert(ProductBarcode barcode);

    void deleteById(ProductBarcodeId barcodeId);
}
