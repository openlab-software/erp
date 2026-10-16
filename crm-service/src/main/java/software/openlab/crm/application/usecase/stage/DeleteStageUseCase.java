package software.openlab.crm.application.usecase.stage;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.crm.domain.shared.ConflictException;
import software.openlab.crm.domain.shared.DomainEvent;
import software.openlab.crm.domain.stage.StageEvents;
import software.openlab.crm.domain.stage.StageId;
import software.openlab.crm.domain.stage.StageRepository;

/** A stage that still holds leads cannot be removed: move or close them first. */
@ApplicationScoped
public class DeleteStageUseCase {

    @Inject
    StageRepository repository;

    @Inject
    GetStageByIdUseCase getStageById;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public void execute(StageId stageId) {
        getStageById.execute(stageId);
        if (repository.hasLeads(stageId)) {
            throw new ConflictException("error.stage.inUse");
        }
        repository.deleteById(stageId);
        events.fire(new DomainEvent(StageEvents.DELETED, new StageEvents.StageDeletedPayload(stageId.toString())));
    }
}
