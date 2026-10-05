package software.openlab.catalog.application.usecase.product;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import software.openlab.catalog.domain.product.ProductBarcode;
import software.openlab.catalog.domain.product.ProductBarcodeRepository;
import software.openlab.catalog.domain.product.ProductId;

@ApplicationScoped
public class GetProductBarcodesUseCase {

    @Inject
    ProductBarcodeRepository repository;

    public List<ProductBarcode> execute(ProductId productId) {
        return repository.findByProductId(productId);
    }
}
