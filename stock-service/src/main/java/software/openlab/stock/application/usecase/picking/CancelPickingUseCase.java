package software.openlab.stock.application.usecase.picking;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.stock.domain.picking.Picking;
import software.openlab.stock.domain.picking.PickingEvents;
import software.openlab.stock.domain.picking.PickingId;
import software.openlab.stock.domain.picking.PickingRepository;
import software.openlab.stock.domain.shared.DomainEvent;

@ApplicationScoped
public class CancelPickingUseCase {

    @Inject
    PickingRepository repository;

    @Inject
    GetPickingByIdUseCase getPickingById;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public Picking execute(PickingId pickingId) {
        Picking picking = getPickingById.execute(pickingId);
        picking.cancel();
        repository.update(picking);
        Picking cancelled = getPickingById.execute(pickingId);
        events.fire(new DomainEvent(PickingEvents.CANCELLED, PickingEvents.PickingPayload.of(cancelled)));
        return cancelled;
    }
}
