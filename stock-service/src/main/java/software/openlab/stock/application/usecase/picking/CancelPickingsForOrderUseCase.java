package software.openlab.stock.application.usecase.picking;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.stock.domain.picking.Picking;
import software.openlab.stock.domain.picking.PickingEvents;
import software.openlab.stock.domain.picking.PickingRepository;
import software.openlab.stock.domain.shared.DomainEvent;

/** Reaction to a cancelled order: its pickings that did not move anything yet are cancelled too. */
@ApplicationScoped
public class CancelPickingsForOrderUseCase {

    @Inject
    PickingRepository repository;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public void execute(String originType, String originId) {
        for (Picking picking : repository.findOpenByOrigin(originType, originId)) {
            picking.cancel();
            repository.update(picking);
            events.fire(new DomainEvent(PickingEvents.CANCELLED, PickingEvents.PickingPayload.of(picking)));
        }
    }
}
