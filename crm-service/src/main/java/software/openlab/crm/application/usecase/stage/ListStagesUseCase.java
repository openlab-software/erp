package software.openlab.crm.application.usecase.stage;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import software.openlab.crm.domain.stage.Stage;
import software.openlab.crm.domain.stage.StageRepository;

@ApplicationScoped
public class ListStagesUseCase {

    @Inject
    StageRepository repository;

    public List<Stage> execute() {
        return repository.findAll();
    }
}
