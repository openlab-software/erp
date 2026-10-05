package software.openlab.catalog.domain.category;

import java.util.Optional;
import software.openlab.catalog.domain.shared.PageResult;

public interface CategoryRepository {

    Optional<Category> findById(CategoryId categoryId);

    boolean existsByDescriptionIgnoreCase(String description, CategoryId excludeCategoryId);

    PageResult<Category> find(String q, int page, int pageSize);

    void insert(Category category);

    void update(Category category);

    void deleteById(CategoryId categoryId);

    boolean hasProducts(CategoryId categoryId);

    boolean hasSubcategories(CategoryId categoryId);

    boolean existsById(CategoryId categoryId);
}
