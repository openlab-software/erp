package software.openlab.catalog.application.usecase.supplier;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.catalog.domain.shared.ConflictException;
import software.openlab.catalog.domain.shared.DomainEvent;
import software.openlab.catalog.domain.shared.Fields;
import software.openlab.catalog.domain.shared.NotFoundException;
import software.openlab.catalog.domain.supplier.Supplier;
import software.openlab.catalog.domain.supplier.SupplierEvents;
import software.openlab.catalog.domain.supplier.SupplierId;
import software.openlab.catalog.domain.supplier.SupplierRepository;

@ApplicationScoped
public class UpdateSupplierUseCase {

    @Inject
    SupplierRepository repository;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public Supplier execute(SupplierId supplierId, String name, String document) {
        String trimmedName = Fields.requireNonBlank(name, "name");
        String trimmedDocument = Fields.requireNonBlank(document, "document");

        Supplier supplier = repository.findById(supplierId)
                .orElseThrow(() -> new NotFoundException("error.supplier.notFound", supplierId));

        if (repository.existsByDocument(trimmedDocument, supplierId)) {
            throw new ConflictException("error.supplier.duplicate");
        }

        supplier.setName(trimmedName);
        supplier.setDocument(trimmedDocument);
        repository.update(supplier);

        events.fire(new DomainEvent(
                SupplierEvents.UPDATED,
                new SupplierEvents.SupplierUpdatedPayload(supplier.getSupplierId().toString(), supplier.getName(), supplier.getDocument())
        ));

        return supplier;
    }
}
