package software.openlab.catalog.application.usecase.category;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.catalog.domain.category.CategoryEvents;
import software.openlab.catalog.domain.category.CategoryId;
import software.openlab.catalog.domain.category.CategoryRepository;
import software.openlab.catalog.domain.shared.ConflictException;
import software.openlab.catalog.domain.shared.DomainEvent;
import software.openlab.catalog.domain.shared.NotFoundException;

@ApplicationScoped
public class DeleteCategoryUseCase {

    @Inject
    CategoryRepository repository;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public void execute(CategoryId categoryId) {
        if (!repository.existsById(categoryId)) {
            throw new NotFoundException("error.category.notFound", categoryId);
        }

        if (repository.hasSubcategories(categoryId)) {
            throw new ConflictException("error.category.hasChildren");
        }

        if (repository.hasProducts(categoryId)) {
            throw new ConflictException("error.category.hasProducts");
        }

        repository.deleteById(categoryId);

        events.fire(new DomainEvent(CategoryEvents.DELETED, new CategoryEvents.CategoryDeletedPayload(categoryId.value())));
    }
}
