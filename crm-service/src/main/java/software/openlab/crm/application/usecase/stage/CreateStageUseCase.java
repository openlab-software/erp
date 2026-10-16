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
import software.openlab.crm.domain.stage.StageRepository;

@ApplicationScoped
public class CreateStageUseCase {

    @Inject
    StageRepository repository;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public Stage execute(String name, Integer position, Integer probability, Boolean won) {
        String cleanName = Fields.requireNonBlank(name, "name");
        if (repository.existsByName(cleanName, null)) {
            throw new ConflictException("error.stage.duplicate", cleanName);
        }
        int pos = position != null ? position : repository.findAll().stream()
                .mapToInt(Stage::getPosition).max().orElse(0) + 10;
        Stage stage = Stage.newStage(cleanName, pos, probability == null ? 0 : probability, Boolean.TRUE.equals(won));
        repository.insert(stage);
        events.fire(new DomainEvent(StageEvents.CREATED, StageEvents.StagePayload.of(stage)));
        return stage;
    }
}
