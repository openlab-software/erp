package software.openlab.catalog.application.usecase.brand;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.catalog.domain.brand.BrandEvents;
import software.openlab.catalog.domain.brand.BrandId;
import software.openlab.catalog.domain.brand.BrandRepository;
import software.openlab.catalog.domain.shared.ConflictException;
import software.openlab.catalog.domain.shared.DomainEvent;
import software.openlab.catalog.domain.shared.NotFoundException;

@ApplicationScoped
public class DeleteBrandUseCase {

    @Inject
    BrandRepository repository;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public void execute(BrandId brandId) {
        if (!repository.existsById(brandId)) {
            throw new NotFoundException("error.brand.notFound", brandId);
        }

        if (repository.hasProducts(brandId)) {
            throw new ConflictException("error.brand.hasProducts");
        }

        repository.deleteById(brandId);

        events.fire(new DomainEvent(BrandEvents.DELETED, new BrandEvents.BrandDeletedPayload(brandId.value())));
    }
}
