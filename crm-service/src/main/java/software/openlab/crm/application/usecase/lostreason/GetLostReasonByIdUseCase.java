package software.openlab.crm.application.usecase.lostreason;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.openlab.crm.domain.lostreason.LostReason;
import software.openlab.crm.domain.lostreason.LostReasonId;
import software.openlab.crm.domain.lostreason.LostReasonRepository;
import software.openlab.crm.domain.shared.NotFoundException;

@ApplicationScoped
public class GetLostReasonByIdUseCase {

    @Inject
    LostReasonRepository repository;

    public LostReason execute(LostReasonId id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("error.lostReason.notFound", id));
    }
}
