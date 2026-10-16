package software.openlab.crm.application.usecase.lostreason;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.crm.domain.lostreason.LostReason;
import software.openlab.crm.domain.lostreason.LostReasonEvents;
import software.openlab.crm.domain.lostreason.LostReasonRepository;
import software.openlab.crm.domain.shared.ConflictException;
import software.openlab.crm.domain.shared.DomainEvent;
import software.openlab.crm.domain.shared.Fields;

@ApplicationScoped
public class CreateLostReasonUseCase {

    @Inject
    LostReasonRepository repository;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public LostReason execute(String name) {
        String cleanName = Fields.requireNonBlank(name, "name");
        if (repository.existsByName(cleanName, null)) {
            throw new ConflictException("error.lostReason.duplicate", cleanName);
        }
        LostReason reason = LostReason.newReason(cleanName);
        repository.insert(reason);
        events.fire(new DomainEvent(LostReasonEvents.CREATED, LostReasonEvents.LostReasonPayload.of(reason)));
        return reason;
    }
}
