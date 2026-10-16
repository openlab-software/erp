package software.openlab.crm.application.usecase.lostreason;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.crm.domain.lostreason.LostReason;
import software.openlab.crm.domain.lostreason.LostReasonEvents;
import software.openlab.crm.domain.lostreason.LostReasonId;
import software.openlab.crm.domain.lostreason.LostReasonRepository;
import software.openlab.crm.domain.shared.ConflictException;
import software.openlab.crm.domain.shared.DomainEvent;
import software.openlab.crm.domain.shared.Fields;

@ApplicationScoped
public class UpdateLostReasonUseCase {

    @Inject
    LostReasonRepository repository;

    @Inject
    GetLostReasonByIdUseCase getById;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public LostReason execute(LostReasonId id, String name, Boolean active) {
        LostReason reason = getById.execute(id);
        String cleanName = Fields.requireNonBlank(name, "name");
        if (repository.existsByName(cleanName, id)) {
            throw new ConflictException("error.lostReason.duplicate", cleanName);
        }
        reason.setName(cleanName);
        if (active != null) {
            reason.setActive(active);
        }
        repository.update(reason);
        LostReason updated = getById.execute(id);
        events.fire(new DomainEvent(LostReasonEvents.UPDATED, LostReasonEvents.LostReasonPayload.of(updated)));
        return updated;
    }
}
