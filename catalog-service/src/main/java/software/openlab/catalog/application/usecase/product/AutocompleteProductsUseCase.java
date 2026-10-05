package software.openlab.catalog.application.usecase.product;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import software.openlab.catalog.domain.product.Product;
import software.openlab.catalog.domain.product.ProductRepository;
import software.openlab.catalog.domain.shared.BadRequestException;

@ApplicationScoped
public class AutocompleteProductsUseCase {

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 20;

    @Inject
    ProductRepository repository;

    public List<Product> execute(String q, Integer limit) {
        String trimmed = q == null ? "" : q.trim();
        if (trimmed.isEmpty()) {
            throw new BadRequestException("error.field.required", "q");
        }

        return repository.autocomplete(trimmed, normalizeLimit(limit));
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
