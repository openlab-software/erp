package software.openlab.accounting.application.usecase.invoice;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.accounting.domain.invoice.Invoice;
import software.openlab.accounting.domain.invoice.InvoiceEvents;
import software.openlab.accounting.domain.invoice.InvoiceId;
import software.openlab.accounting.domain.invoice.InvoiceRepository;
import software.openlab.accounting.domain.invoice.InvoiceState;
import software.openlab.accounting.domain.shared.ConflictException;
import software.openlab.accounting.domain.shared.DomainEvent;

/** A numbered invoice is a fiscal document: only drafts that never got a number can be deleted. */
@ApplicationScoped
public class DeleteInvoiceUseCase {

    @Inject
    InvoiceRepository repository;

    @Inject
    GetInvoiceByIdUseCase getInvoiceById;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public void execute(InvoiceId invoiceId) {
        Invoice invoice = getInvoiceById.execute(invoiceId);
        if (invoice.getState() != InvoiceState.DRAFT || invoice.getNumber() != null) {
            throw new ConflictException("error.invoice.notDeletable", invoice.getState());
        }
        repository.deleteById(invoiceId);
        events.fire(new DomainEvent(InvoiceEvents.DELETED,
                new InvoiceEvents.InvoiceDeletedPayload(invoiceId.toString())));
    }
}
