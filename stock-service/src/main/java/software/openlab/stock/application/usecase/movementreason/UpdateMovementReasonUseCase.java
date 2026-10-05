package software.openlab.stock.application.usecase.movementreason;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.stock.domain.movementreason.MovementReason;
import software.openlab.stock.domain.movementreason.MovementReasonEvents;
import software.openlab.stock.domain.movementreason.MovementReasonId;
import software.openlab.stock.domain.movementreason.MovementReasonRepository;
import software.openlab.stock.domain.movementreason.MovementReasonUpdatedPayload;
import software.openlab.stock.domain.shared.ConflictException;
import software.openlab.stock.domain.shared.DomainEvent;
import software.openlab.stock.domain.shared.NotFoundException;

@ApplicationScoped
public class UpdateMovementReasonUseCase {

    @Inject
    MovementReasonRepository movementReasonRepository;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public MovementReason execute(MovementReasonId reasonId, String description) {
        MovementReason reason = movementReasonRepository.findById(reasonId)
                .orElseThrow(() -> new NotFoundException("error.movementReason.notFound"));

        String trimmed = description.trim();
        if (movementReasonRepository.existsByDescriptionIgnoreCaseExcluding(trimmed, reasonId)) {
            throw new ConflictException("error.movementReason.duplicate");
        }

        reason.rename(trimmed);
        movementReasonRepository.update(reason);
        events.fire(new DomainEvent(MovementReasonEvents.UPDATED,
                new MovementReasonUpdatedPayload(reason.getReasonId().toString(), reason.getDescription())));
        return reason;
    }
}
