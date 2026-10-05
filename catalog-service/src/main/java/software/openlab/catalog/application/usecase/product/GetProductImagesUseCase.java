package software.openlab.catalog.application.usecase.product;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import software.openlab.catalog.domain.product.ProductId;
import software.openlab.catalog.domain.product.ProductImage;
import software.openlab.catalog.domain.product.ProductImageRepository;

@ApplicationScoped
public class GetProductImagesUseCase {

    @Inject
    ProductImageRepository repository;

    public List<ProductImage> execute(ProductId productId) {
        return repository.findByProductId(productId);
    }
}
