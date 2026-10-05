package software.openlab.catalog.application.usecase.category;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.catalog.domain.category.Category;
import software.openlab.catalog.domain.category.CategoryEvents;
import software.openlab.catalog.domain.category.CategoryId;
import software.openlab.catalog.domain.category.CategoryRepository;
import software.openlab.catalog.domain.shared.BadRequestException;
import software.openlab.catalog.domain.shared.ConflictException;
import software.openlab.catalog.domain.shared.DomainEvent;
import software.openlab.catalog.domain.shared.Fields;

@ApplicationScoped
public class UpdateCategoryUseCase {

    @Inject
    CategoryRepository repository;

    @Inject
    Event<DomainEvent> events;

    @Inject
    GetCategoryByIdUseCase getCategoryById;

    @Transactional
    public Category execute(CategoryId categoryId, String description, CategoryId parentCategoryId) {
        String trimmed = Fields.requireNonBlank(description, "description");

        Category category = getCategoryById.execute(categoryId);

        if (repository.existsByDescriptionIgnoreCase(trimmed, categoryId)) {
            throw new ConflictException("error.category.duplicate");
        }

        if (parentCategoryId != null) {
            if (!repository.existsById(parentCategoryId)) {
                throw new BadRequestException("error.ref.category", "parent_category_id");
            }
            assertNoCycle(categoryId, parentCategoryId);
        }

        category.setDescription(trimmed);
        category.setParentCategoryId(parentCategoryId);
        repository.update(category);

        events.fire(
                new DomainEvent(CategoryEvents.UPDATED,
                new CategoryEvents.CategoryUpdatedPayload(
                        category.getCategoryId().toString(),
                        category.getDescription(),
                        category.getParentCategoryId() != null ? category.getParentCategoryId().toString() : null))
        );

        return category;
    }

    /**
     * Walking up from the proposed parent must never reach {@code categoryId}
     * itself: reaching it at depth 0 is self-reference, at any deeper depth
     * it means the proposed parent is a descendant of the category being
     * updated, which would close a cycle in the tree.
     */
    private void assertNoCycle(CategoryId categoryId, CategoryId proposedParentId) {
        CategoryId current = proposedParentId;
        while (current != null) {
            if (current.equals(categoryId)) {
                throw new ConflictException("error.category.cycle", "parent_category_id");
            }
            current = getCategoryById.execute(current).getParentCategoryId();
        }
    }
}
