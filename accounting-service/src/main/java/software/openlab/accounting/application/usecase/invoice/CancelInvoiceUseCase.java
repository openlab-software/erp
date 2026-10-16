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
public class CancelInvoiceUseCase {

    @Inject
    InvoiceRepository repository;

    @Inject
    GetInvoiceByIdUseCase getInvoiceById;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public Invoice execute(InvoiceId invoiceId) {
        Invoice invoice = getInvoiceById.execute(invoiceId);
        invoice.cancel();
        repository.update(invoice);
        Invoice cancelled = getInvoiceById.execute(invoiceId);
        events.fire(new DomainEvent(InvoiceEvents.CANCELLED, InvoiceEvents.InvoicePayload.of(cancelled)));
        return cancelled;
    }
}
