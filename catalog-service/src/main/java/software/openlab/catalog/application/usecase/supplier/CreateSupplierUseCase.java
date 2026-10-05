package software.openlab.catalog.application.usecase.supplier;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.catalog.domain.shared.ConflictException;
import software.openlab.catalog.domain.shared.DomainEvent;
import software.openlab.catalog.domain.shared.Fields;
import software.openlab.catalog.domain.supplier.Supplier;
import software.openlab.catalog.domain.supplier.SupplierEvents;
import software.openlab.catalog.domain.supplier.SupplierRepository;

@ApplicationScoped
public class CreateSupplierUseCase {

    @Inject
    SupplierRepository repository;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public Supplier execute(String name, String document) {
        String trimmedName = Fields.requireNonBlank(name, "name");
        String trimmedDocument = Fields.requireNonBlank(document, "document");

        if (repository.existsByDocument(trimmedDocument, null)) {
            throw new ConflictException("error.supplier.duplicate");
        }

        Supplier supplier = Supplier.newSupplier(trimmedName, trimmedDocument);
        repository.insert(supplier);

        events.fire(new DomainEvent(
                SupplierEvents.CREATED,
                new SupplierEvents.SupplierCreatedPayload(supplier.getSupplierId().toString(), supplier.getName(), supplier.getDocument())
        ));

        return supplier;
    }
}
