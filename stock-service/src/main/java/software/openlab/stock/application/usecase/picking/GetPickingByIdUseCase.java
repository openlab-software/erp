package software.openlab.stock.application.usecase.picking;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.openlab.stock.domain.picking.Picking;
import software.openlab.stock.domain.picking.PickingId;
import software.openlab.stock.domain.picking.PickingRepository;
import software.openlab.stock.domain.shared.NotFoundException;

@ApplicationScoped
public class GetPickingByIdUseCase {

    @Inject
    PickingRepository repository;

    public Picking execute(PickingId pickingId) {
        return repository.findById(pickingId)
                .orElseThrow(() -> new NotFoundException("error.picking.notFound", pickingId));
    }
}
