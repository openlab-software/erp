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

/**
 * Posts a draft: it gets its number (kept if it had one before being cancelled) and becomes read-only.
 * {@code invoice.posted} tells the originating module (sales, purchase) that the order was invoiced.
 */
@ApplicationScoped
public class PostInvoiceUseCase {

    @Inject
    InvoiceRepository repository;

    @Inject
    GetInvoiceByIdUseCase getInvoiceById;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public Invoice execute(InvoiceId invoiceId) {
        Invoice invoice = getInvoiceById.execute(invoiceId);
        String number = invoice.getNumber() != null ? invoice.getNumber() : repository.nextNumber(invoice.getKind());
        invoice.post(number);
        repository.update(invoice);
        Invoice posted = getInvoiceById.execute(invoiceId);
        events.fire(new DomainEvent(InvoiceEvents.POSTED, InvoiceEvents.InvoicePayload.of(posted)));
        return posted;
    }
}
