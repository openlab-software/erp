package software.openlab.catalog.application.usecase.brand;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.catalog.domain.brand.Brand;
import software.openlab.catalog.domain.brand.BrandEvents;
import software.openlab.catalog.domain.brand.BrandRepository;
import software.openlab.catalog.domain.shared.ConflictException;
import software.openlab.catalog.domain.shared.DomainEvent;
import software.openlab.catalog.domain.shared.Fields;

@ApplicationScoped
public class CreateBrandUseCase {

    @Inject
    BrandRepository repository;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public Brand execute(String description) {
        String trimmed = Fields.requireNonBlank(description, "description");

        if (repository.existsByDescriptionIgnoreCase(trimmed, null)) {
            throw new ConflictException("error.brand.duplicate");
        }

        Brand brand = Brand.newBrand(trimmed);
        repository.insert(brand);

        events.fire(new DomainEvent(
                BrandEvents.CREATED,
                new BrandEvents.BrandCreatedPayload(brand.getBrandId().toString(), brand.getDescription())
        ));

        return brand;
    }
}
