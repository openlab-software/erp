package software.openlab.crm.application.usecase.stage;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.crm.domain.shared.ConflictException;
import software.openlab.crm.domain.shared.DomainEvent;
import software.openlab.crm.domain.shared.Fields;
import software.openlab.crm.domain.stage.Stage;
import software.openlab.crm.domain.stage.StageEvents;
import software.openlab.crm.domain.stage.StageId;
import software.openlab.crm.domain.stage.StageRepository;

@ApplicationScoped
public class UpdateStageUseCase {

    @Inject
    StageRepository repository;

    @Inject
    GetStageByIdUseCase getStageById;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public Stage execute(StageId stageId, String name, int position, int probability, boolean won) {
        Stage stage = getStageById.execute(stageId);
        String cleanName = Fields.requireNonBlank(name, "name");
        if (repository.existsByName(cleanName, stageId)) {
            throw new ConflictException("error.stage.duplicate", cleanName);
        }
        stage.apply(cleanName, position, probability, won);
        repository.update(stage);
        Stage updated = getStageById.execute(stageId);
        events.fire(new DomainEvent(StageEvents.UPDATED, StageEvents.StagePayload.of(updated)));
        return updated;
    }
}
