package software.openlab.catalog.application.usecase.unitofmeasure;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.catalog.domain.shared.ConflictException;
import software.openlab.catalog.domain.shared.DomainEvent;
import software.openlab.catalog.domain.shared.NotFoundException;
import software.openlab.catalog.domain.unitofmeasure.UnitOfMeasureEvents;
import software.openlab.catalog.domain.unitofmeasure.UnitOfMeasureId;
import software.openlab.catalog.domain.unitofmeasure.UnitOfMeasureRepository;

@ApplicationScoped
public class DeleteUnitOfMeasureUseCase {

    @Inject
    UnitOfMeasureRepository repository;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public void execute(UnitOfMeasureId unitOfMeasureId) {
        if (!repository.existsById(unitOfMeasureId)) {
            throw new NotFoundException("error.unitOfMeasure.notFound", unitOfMeasureId);
        }

        if (repository.hasProducts(unitOfMeasureId)) {
            throw new ConflictException("error.unitOfMeasure.hasProducts");
        }

        repository.deleteById(unitOfMeasureId);

        events.fire(new DomainEvent(UnitOfMeasureEvents.DELETED, new UnitOfMeasureEvents.UnitOfMeasureDeletedPayload(unitOfMeasureId.value())));
    }
}
