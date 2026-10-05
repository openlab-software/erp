package software.openlab.stock.application.usecase.movementreason;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.stock.domain.movementreason.MovementReason;
import software.openlab.stock.domain.movementreason.MovementReasonCreatedPayload;
import software.openlab.stock.domain.movementreason.MovementReasonEvents;
import software.openlab.stock.domain.movementreason.MovementReasonRepository;
import software.openlab.stock.domain.shared.ConflictException;
import software.openlab.stock.domain.shared.DomainEvent;

@ApplicationScoped
public class CreateMovementReasonUseCase {

    @Inject
    MovementReasonRepository movementReasonRepository;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public MovementReason execute(String description) {
        String trimmed = description.trim();
        if (movementReasonRepository.existsByDescriptionIgnoreCase(trimmed)) {
            throw new ConflictException("error.movementReason.duplicate");
        }

        MovementReason reason = MovementReason.create(trimmed);
        movementReasonRepository.insert(reason);
        events.fire(new DomainEvent(
                MovementReasonEvents.CREATED,
                new MovementReasonCreatedPayload(reason.getReasonId().toString(), reason.getDescription())
        ));
        return reason;
    }
}
