package software.openlab.catalog.application.usecase.category;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.openlab.catalog.domain.category.Category;
import software.openlab.catalog.domain.category.CategoryRepository;
import software.openlab.catalog.domain.shared.PageResult;

@ApplicationScoped
public class ListCategoriesUseCase {

    @Inject
    CategoryRepository repository;

    public PageResult<Category> execute(String q, Integer page, Integer pageSize) {
        int normalizedPage = PageResult.normalizePage(page);
        int normalizedPageSize = PageResult.normalizePageSize(pageSize);
        return repository.find(q, normalizedPage, normalizedPageSize);
    }
}
