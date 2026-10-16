package software.openlab.accounting.application.usecase.invoice;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.accounting.domain.invoice.Invoice;
import software.openlab.accounting.domain.invoice.InvoiceCommand;
import software.openlab.accounting.domain.invoice.InvoiceEvents;
import software.openlab.accounting.domain.invoice.InvoiceId;
import software.openlab.accounting.domain.invoice.InvoiceRepository;
import software.openlab.accounting.domain.shared.DomainEvent;

@ApplicationScoped
public class UpdateInvoiceUseCase {

    @Inject
    InvoiceRepository repository;

    @Inject
    GetInvoiceByIdUseCase getInvoiceById;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public Invoice execute(InvoiceId invoiceId, InvoiceCommand raw) {
        Invoice invoice = getInvoiceById.execute(invoiceId);
        invoice.apply(raw.validated(invoice.getKind()));
        repository.update(invoice);
        Invoice updated = getInvoiceById.execute(invoiceId);
        events.fire(new DomainEvent(InvoiceEvents.UPDATED, InvoiceEvents.InvoicePayload.of(updated)));
        return updated;
    }
}
