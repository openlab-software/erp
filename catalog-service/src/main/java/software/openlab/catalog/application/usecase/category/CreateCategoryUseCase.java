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
public class CreateCategoryUseCase {

    @Inject
    CategoryRepository repository;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public Category execute(String description, CategoryId parentCategoryId) {
        String trimmed = Fields.requireNonBlank(description, "description");

        if (repository.existsByDescriptionIgnoreCase(trimmed, null)) {
            throw new ConflictException("error.category.duplicate");
        }

        if (parentCategoryId != null && !repository.existsById(parentCategoryId)) {
            throw new BadRequestException("error.ref.category", "parent_category_id");
        }

        Category category = Category.newCategory(trimmed, parentCategoryId);
        repository.insert(category);

        events.fire(new DomainEvent(
                CategoryEvents.CREATED,
                new CategoryEvents.CategoryCreatedPayload(
                        category.getCategoryId().toString(),
                        category.getDescription(),
                        category.getParentCategoryId() != null ? category.getParentCategoryId().toString() : null)
        ));

        return category;
    }
}
