package software.openlab.stock.application.usecase.picking;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.stock.application.usecase.stock.GetStockByIdUseCase;
import software.openlab.stock.domain.picking.Picking;
import software.openlab.stock.domain.picking.PickingCommand;
import software.openlab.stock.domain.picking.PickingEvents;
import software.openlab.stock.domain.picking.PickingRepository;
import software.openlab.stock.domain.picking.PickingType;
import software.openlab.stock.domain.shared.DomainEvent;

/** Creates a draft receipt, delivery or internal transfer by hand. */
@ApplicationScoped
public class CreatePickingUseCase {

    @Inject
    PickingRepository repository;

    @Inject
    GetStockByIdUseCase getStockById;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public Picking execute(String rawType, PickingCommand raw) {
        PickingType type = PickingType.parse(rawType);
        PickingCommand command = raw.validated(type);
        checkStocks(command);

        Picking picking = Picking.newPicking(repository.nextNumber(type), type, command);
        repository.insert(picking);
        events.fire(new DomainEvent(PickingEvents.CREATED, PickingEvents.PickingPayload.of(picking)));
        return picking;
    }

    /** 404 when a given stock does not exist. */
    void checkStocks(PickingCommand command) {
        if (command.sourceStockId() != null) {
            getStockById.execute(command.sourceStockId());
        }
        if (command.destinationStockId() != null) {
            getStockById.execute(command.destinationStockId());
        }
    }
}
