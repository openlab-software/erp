package software.openlab.catalog.application.usecase.product;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.openlab.catalog.domain.product.Product;
import software.openlab.catalog.domain.product.ProductId;
import software.openlab.catalog.domain.product.ProductRepository;
import software.openlab.catalog.domain.shared.NotFoundException;

@ApplicationScoped
public class GetProductByIdUseCase {

    @Inject
    ProductRepository repository;

    public Product execute(ProductId productId) {
        return repository.findById(productId)
                .orElseThrow(() -> new NotFoundException("error.product.notFound", productId));
    }
}
