package software.openlab.stock.application.usecase.picking;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.stock.domain.picking.Picking;
import software.openlab.stock.domain.picking.PickingEvents;
import software.openlab.stock.domain.picking.PickingId;
import software.openlab.stock.domain.picking.PickingRepository;
import software.openlab.stock.domain.picking.PickingState;
import software.openlab.stock.domain.shared.ConflictException;
import software.openlab.stock.domain.shared.DomainEvent;

/** A validated picking is part of the stock history: only drafts and cancelled ones can be deleted. */
@ApplicationScoped
public class DeletePickingUseCase {

    @Inject
    PickingRepository repository;

    @Inject
    GetPickingByIdUseCase getPickingById;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public void execute(PickingId pickingId) {
        Picking picking = getPickingById.execute(pickingId);
        if (picking.getState() != PickingState.DRAFT && picking.getState() != PickingState.CANCELLED) {
            throw new ConflictException("error.picking.notDeletable", picking.getState());
        }
        repository.deleteById(pickingId);
        events.fire(new DomainEvent(PickingEvents.DELETED,
                new PickingEvents.PickingDeletedPayload(pickingId.toString())));
    }
}
