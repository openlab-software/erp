package software.openlab.catalog.application.usecase.brand;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.catalog.domain.brand.Brand;
import software.openlab.catalog.domain.brand.BrandEvents;
import software.openlab.catalog.domain.brand.BrandId;
import software.openlab.catalog.domain.brand.BrandRepository;
import software.openlab.catalog.domain.shared.ConflictException;
import software.openlab.catalog.domain.shared.DomainEvent;
import software.openlab.catalog.domain.shared.Fields;
import software.openlab.catalog.domain.shared.NotFoundException;

@ApplicationScoped
public class UpdateBrandUseCase {

    @Inject
    BrandRepository repository;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public Brand execute(BrandId brandId, String description) {
        String trimmed = Fields.requireNonBlank(description, "description");

        Brand brand = repository.findById(brandId)
                .orElseThrow(() -> new NotFoundException("error.brand.notFound", brandId));

        if (repository.existsByDescriptionIgnoreCase(trimmed, brandId)) {
            throw new ConflictException("error.brand.duplicate");
        }

        brand.setDescription(trimmed);
        repository.update(brand);

        events.fire(new DomainEvent(
                BrandEvents.UPDATED,
                new BrandEvents.BrandUpdatedPayload(brand.getBrandId().toString(), brand.getDescription())
        ));

        return brand;
    }
}
