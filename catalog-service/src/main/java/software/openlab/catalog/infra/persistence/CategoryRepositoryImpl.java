package software.openlab.catalog.infra.persistence;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import software.openlab.catalog.domain.category.Category;
import software.openlab.catalog.domain.category.CategoryId;
import software.openlab.catalog.domain.category.CategoryRepository;
import software.openlab.catalog.domain.shared.PageResult;

@ApplicationScoped
public class CategoryRepositoryImpl implements CategoryRepository {

    @Override
    public Optional<Category> findById(CategoryId categoryId) {
        return CategoryEntity.<CategoryEntity>find("publicId", categoryId.toString())
                .firstResultOptional()
                .map(this::toDomain);
    }

    @Override
    public boolean existsByDescriptionIgnoreCase(String description, CategoryId excludeCategoryId) {
        if (excludeCategoryId == null) {
            return CategoryEntity.count("lower(description) = lower(?1)", description) > 0;
        }
        return CategoryEntity.count("lower(description) = lower(?1) and publicId <> ?2", description, excludeCategoryId.toString()) > 0;
    }

    @Override
    public PageResult<Category> find(String q, int page, int pageSize) {
        Sort sort = Sort.by("createdAt", Sort.Direction.Descending);

        var query = (q == null || q.isBlank())
                ? CategoryEntity.find("", sort)
                : CategoryEntity.find("lower(description) like lower(?1)", sort, "%" + q + "%");

        long total = query.count();
        List<Category> data = query.page(Page.of(page - 1, pageSize))
                .<CategoryEntity>list()
                .stream()
                .map(this::toDomain)
                .toList();

        return new PageResult<>(data, page, pageSize, total);
    }

    @Override
    public void insert(Category category) {
        CategoryEntity entity = new CategoryEntity();
        entity.publicId = category.getCategoryId().toString();
        entity.description = category.getDescription();
        entity.parent = resolveParent(category.getParentCategoryId());
        entity.createdAt = category.getCreatedAt();
        CategoryEntity.persist(entity);
    }

    @Override
    public void update(Category category) {
        CategoryEntity entity = CategoryEntity.<CategoryEntity>find("publicId", category.getCategoryId().toString())
                .firstResultOptional()
                .orElseThrow(() -> new IllegalStateException("category not found: " + category.getCategoryId()));
        entity.description = category.getDescription();
        entity.parent = resolveParent(category.getParentCategoryId());
        entity.updatedAt = Instant.now();
    }

    @Override
    public void deleteById(CategoryId categoryId) {
        CategoryEntity.delete("publicId", categoryId.toString());
    }

    @Override
    public boolean hasProducts(CategoryId categoryId) {
        return ProductEntity.count("category.publicId = ?1", categoryId.toString()) > 0;
    }

    @Override
    public boolean hasSubcategories(CategoryId categoryId) {
        return CategoryEntity.count("parent.publicId = ?1", categoryId.toString()) > 0;
    }

    @Override
    public boolean existsById(CategoryId categoryId) {
        return CategoryEntity.count("publicId", categoryId.toString()) > 0;
    }

    private CategoryEntity resolveParent(CategoryId parentCategoryId) {
        if (parentCategoryId == null) {
            return null;
        }
        return CategoryEntity.<CategoryEntity>find("publicId", parentCategoryId.toString())
                .firstResultOptional()
                .orElseThrow(() -> new IllegalStateException("parent category not found: " + parentCategoryId));
    }

    private Category toDomain(CategoryEntity e) {
        CategoryId parentCategoryId = e.parent != null ? CategoryId.of(e.parent.publicId) : null;
        return new Category(CategoryId.of(e.publicId), e.description, parentCategoryId, e.createdAt, e.updatedAt);
    }
}
