package software.openlab.stock.application.usecase.movementreason;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.stock.domain.movement.StockMovementRepository;
import software.openlab.stock.domain.movementreason.MovementReasonDeletedPayload;
import software.openlab.stock.domain.movementreason.MovementReasonEvents;
import software.openlab.stock.domain.movementreason.MovementReasonId;
import software.openlab.stock.domain.movementreason.MovementReasonRepository;
import software.openlab.stock.domain.shared.ConflictException;
import software.openlab.stock.domain.shared.DomainEvent;
import software.openlab.stock.domain.shared.NotFoundException;

@ApplicationScoped
public class DeleteMovementReasonUseCase {

    @Inject
    MovementReasonRepository movementReasonRepository;

    @Inject
    StockMovementRepository stockMovementRepository;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public void execute(MovementReasonId reasonId) {
        movementReasonRepository.findById(reasonId)
                .orElseThrow(() -> new NotFoundException("error.movementReason.notFound"));

        if (stockMovementRepository.existsByReasonId(reasonId)) {
            throw new ConflictException(
                    "error.movementReason.inUse");
        }

        movementReasonRepository.delete(reasonId);
        events.fire(new DomainEvent(MovementReasonEvents.DELETED,
                new MovementReasonDeletedPayload(reasonId.toString())));
    }
}
