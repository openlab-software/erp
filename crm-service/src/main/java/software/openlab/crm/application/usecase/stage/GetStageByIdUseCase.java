package software.openlab.crm.application.usecase.stage;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.openlab.crm.domain.shared.NotFoundException;
import software.openlab.crm.domain.stage.Stage;
import software.openlab.crm.domain.stage.StageId;
import software.openlab.crm.domain.stage.StageRepository;

@ApplicationScoped
public class GetStageByIdUseCase {

    @Inject
    StageRepository repository;

    public Stage execute(StageId stageId) {
        return repository.findById(stageId).orElseThrow(() -> new NotFoundException("error.stage.notFound", stageId));
    }
}
