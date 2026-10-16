package software.openlab.accounting.application.usecase.invoice;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.accounting.domain.invoice.Invoice;
import software.openlab.accounting.domain.invoice.InvoiceCommand;
import software.openlab.accounting.domain.invoice.InvoiceEvents;
import software.openlab.accounting.domain.invoice.InvoiceKind;
import software.openlab.accounting.domain.invoice.InvoiceRepository;
import software.openlab.accounting.domain.shared.DomainEvent;

/** Creates a draft invoice or vendor bill by hand. */
@ApplicationScoped
public class CreateInvoiceUseCase {

    @Inject
    InvoiceRepository repository;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public Invoice execute(String kind, InvoiceCommand raw) {
        InvoiceKind invoiceKind = InvoiceKind.parse(kind);
        Invoice invoice = Invoice.newDraft(invoiceKind, raw.validated(invoiceKind));
        repository.insert(invoice);
        events.fire(new DomainEvent(InvoiceEvents.CREATED, InvoiceEvents.InvoicePayload.of(invoice)));
        return invoice;
    }
}
