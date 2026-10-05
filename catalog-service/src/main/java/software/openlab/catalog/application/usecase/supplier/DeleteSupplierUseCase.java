package software.openlab.catalog.application.usecase.supplier;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.catalog.domain.shared.ConflictException;
import software.openlab.catalog.domain.shared.DomainEvent;
import software.openlab.catalog.domain.shared.NotFoundException;
import software.openlab.catalog.domain.supplier.SupplierEvents;
import software.openlab.catalog.domain.supplier.SupplierId;
import software.openlab.catalog.domain.supplier.SupplierRepository;

@ApplicationScoped
public class DeleteSupplierUseCase {

    @Inject
    SupplierRepository repository;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public void execute(SupplierId supplierId) {
        if (!repository.existsById(supplierId)) {
            throw new NotFoundException("error.supplier.notFound", supplierId);
        }

        if (repository.hasProducts(supplierId)) {
            throw new ConflictException("error.supplier.hasProducts");
        }

        repository.deleteById(supplierId);

        events.fire(new DomainEvent(SupplierEvents.DELETED, new SupplierEvents.SupplierDeletedPayload(supplierId.value())));
    }
}
