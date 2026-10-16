package software.openlab.stock.application.usecase.picking;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.stock.application.usecase.stock.GetStockByIdUseCase;
import software.openlab.stock.domain.picking.Picking;
import software.openlab.stock.domain.picking.PickingCommand;
import software.openlab.stock.domain.picking.PickingEvents;
import software.openlab.stock.domain.picking.PickingId;
import software.openlab.stock.domain.picking.PickingRepository;
import software.openlab.stock.domain.shared.DomainEvent;

@ApplicationScoped
public class UpdatePickingUseCase {

    @Inject
    PickingRepository repository;

    @Inject
    GetPickingByIdUseCase getPickingById;

    @Inject
    GetStockByIdUseCase getStockById;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public Picking execute(PickingId pickingId, PickingCommand raw) {
        Picking picking = getPickingById.execute(pickingId);
        PickingCommand command = raw.validated(picking.getType());
        if (command.sourceStockId() != null) {
            getStockById.execute(command.sourceStockId());
        }
        if (command.destinationStockId() != null) {
            getStockById.execute(command.destinationStockId());
        }
        picking.apply(command);
        repository.update(picking);
        Picking updated = getPickingById.execute(pickingId);
        events.fire(new DomainEvent(PickingEvents.UPDATED, PickingEvents.PickingPayload.of(updated)));
        return updated;
    }
}
