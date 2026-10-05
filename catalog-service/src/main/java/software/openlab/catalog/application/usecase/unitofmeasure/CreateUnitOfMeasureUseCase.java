package software.openlab.catalog.application.usecase.unitofmeasure;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.catalog.domain.shared.ConflictException;
import software.openlab.catalog.domain.shared.DomainEvent;
import software.openlab.catalog.domain.shared.Fields;
import software.openlab.catalog.domain.unitofmeasure.UnitOfMeasure;
import software.openlab.catalog.domain.unitofmeasure.UnitOfMeasureEvents;
import software.openlab.catalog.domain.unitofmeasure.UnitOfMeasureRepository;

@ApplicationScoped
public class CreateUnitOfMeasureUseCase {

    @Inject
    UnitOfMeasureRepository repository;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public UnitOfMeasure execute(String code, String description) {
        String trimmedCode = Fields.requireNonBlank(code, "code");
        String trimmedDescription = Fields.requireNonBlank(description, "description");

        if (repository.existsByCodeIgnoreCase(trimmedCode, null)) {
            throw new ConflictException("error.unitOfMeasure.duplicate");
        }

        UnitOfMeasure unitOfMeasure = UnitOfMeasure.newUnitOfMeasure(trimmedCode, trimmedDescription);
        repository.insert(unitOfMeasure);

        events.fire(new DomainEvent(
                UnitOfMeasureEvents.CREATED,
                new UnitOfMeasureEvents.UnitOfMeasureCreatedPayload(
                        unitOfMeasure.getUnitOfMeasureId().toString(), unitOfMeasure.getCode(), unitOfMeasure.getDescription())
        ));

        return unitOfMeasure;
    }
}
