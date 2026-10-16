package software.openlab.crm.application.usecase.lostreason;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import software.openlab.crm.domain.lostreason.LostReason;
import software.openlab.crm.domain.lostreason.LostReasonRepository;

@ApplicationScoped
public class ListLostReasonsUseCase {

    @Inject
    LostReasonRepository repository;

    public List<LostReason> execute() {
        return repository.findAll();
    }
}
