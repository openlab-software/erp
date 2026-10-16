package software.openlab.accounting.application.usecase.invoice;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.accounting.domain.invoice.Invoice;
import software.openlab.accounting.domain.invoice.InvoiceEvents;
import software.openlab.accounting.domain.invoice.InvoiceId;
import software.openlab.accounting.domain.invoice.InvoiceRepository;
import software.openlab.accounting.domain.shared.DomainEvent;

@ApplicationScoped
public class ResetInvoiceToDraftUseCase {

    @Inject
    InvoiceRepository repository;

    @Inject
    GetInvoiceByIdUseCase getInvoiceById;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public Invoice execute(InvoiceId invoiceId) {
        Invoice invoice = getInvoiceById.execute(invoiceId);
        invoice.resetToDraft();
        repository.update(invoice);
        Invoice updated = getInvoiceById.execute(invoiceId);
        events.fire(new DomainEvent(InvoiceEvents.UPDATED, InvoiceEvents.InvoicePayload.of(updated)));
        return updated;
    }
}
