package software.openlab.catalog.application.usecase.unitofmeasure;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.catalog.domain.shared.ConflictException;
import software.openlab.catalog.domain.shared.DomainEvent;
import software.openlab.catalog.domain.shared.Fields;
import software.openlab.catalog.domain.shared.NotFoundException;
import software.openlab.catalog.domain.unitofmeasure.UnitOfMeasure;
import software.openlab.catalog.domain.unitofmeasure.UnitOfMeasureEvents;
import software.openlab.catalog.domain.unitofmeasure.UnitOfMeasureId;
import software.openlab.catalog.domain.unitofmeasure.UnitOfMeasureRepository;

@ApplicationScoped
public class UpdateUnitOfMeasureUseCase {

    @Inject
    UnitOfMeasureRepository repository;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public UnitOfMeasure execute(UnitOfMeasureId unitOfMeasureId, String code, String description) {
        String trimmedCode = Fields.requireNonBlank(code, "code");
        String trimmedDescription = Fields.requireNonBlank(description, "description");

        UnitOfMeasure unitOfMeasure = repository.findById(unitOfMeasureId)
                .orElseThrow(() -> new NotFoundException("error.unitOfMeasure.notFound", unitOfMeasureId));

        if (repository.existsByCodeIgnoreCase(trimmedCode, unitOfMeasureId)) {
            throw new ConflictException("error.unitOfMeasure.duplicate");
        }

        unitOfMeasure.setCode(trimmedCode);
        unitOfMeasure.setDescription(trimmedDescription);
        repository.update(unitOfMeasure);

        events.fire(new DomainEvent(
                UnitOfMeasureEvents.UPDATED,
                new UnitOfMeasureEvents.UnitOfMeasureUpdatedPayload(
                        unitOfMeasure.getUnitOfMeasureId().toString(), unitOfMeasure.getCode(), unitOfMeasure.getDescription())
        ));

        return unitOfMeasure;
    }
}
