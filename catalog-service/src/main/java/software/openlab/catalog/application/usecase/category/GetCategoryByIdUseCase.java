package software.openlab.catalog.application.usecase.category;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.openlab.catalog.domain.category.Category;
import software.openlab.catalog.domain.category.CategoryId;
import software.openlab.catalog.domain.category.CategoryRepository;
import software.openlab.catalog.domain.shared.NotFoundException;

@ApplicationScoped
public class GetCategoryByIdUseCase {

    @Inject
    CategoryRepository repository;

    public Category execute(CategoryId categoryId) {
        return repository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("error.category.notFound", categoryId));
    }
}
