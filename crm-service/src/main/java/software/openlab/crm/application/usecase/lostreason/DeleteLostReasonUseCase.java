package software.openlab.crm.application.usecase.lostreason;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.crm.domain.lostreason.LostReasonEvents;
import software.openlab.crm.domain.lostreason.LostReasonId;
import software.openlab.crm.domain.lostreason.LostReasonRepository;
import software.openlab.crm.domain.shared.ConflictException;
import software.openlab.crm.domain.shared.DomainEvent;

/** A reason already used by a lost lead can only be deactivated, not deleted. */
@ApplicationScoped
public class DeleteLostReasonUseCase {

    @Inject
    LostReasonRepository repository;

    @Inject
    GetLostReasonByIdUseCase getById;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public void execute(LostReasonId id) {
        getById.execute(id);
        if (repository.isUsed(id)) {
            throw new ConflictException("error.lostReason.inUse");
        }
        repository.deleteById(id);
        events.fire(new DomainEvent(LostReasonEvents.DELETED,
                new LostReasonEvents.LostReasonDeletedPayload(id.toString())));
    }
}
