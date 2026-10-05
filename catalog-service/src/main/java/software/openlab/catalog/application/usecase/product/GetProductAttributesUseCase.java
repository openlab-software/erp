package software.openlab.catalog.application.usecase.product;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import software.openlab.catalog.domain.product.ProductAttribute;
import software.openlab.catalog.domain.product.ProductAttributeRepository;
import software.openlab.catalog.domain.product.ProductId;

@ApplicationScoped
public class GetProductAttributesUseCase {

    @Inject
    ProductAttributeRepository repository;

    public List<ProductAttribute> execute(ProductId productId) {
        return repository.findByProductId(productId);
    }
}
