package software.openlab.accounting.application.usecase.invoice;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import software.openlab.accounting.domain.invoice.Invoice;
import software.openlab.accounting.domain.invoice.InvoiceCommand;
import software.openlab.accounting.domain.invoice.InvoiceEvents;
import software.openlab.accounting.domain.invoice.InvoiceKind;
import software.openlab.accounting.domain.invoice.InvoiceLine;
import software.openlab.accounting.domain.invoice.InvoiceRepository;
import software.openlab.accounting.domain.shared.DomainEvent;
import software.openlab.accounting.domain.shared.ProcessedEventRepository;

/**
 * Reaction to a confirmed sales or purchase order: drafts the matching invoice / vendor bill. It stays a draft
 * so that accounting reviews and posts it. Idempotent per originating document.
 */
@ApplicationScoped
public class CreateInvoiceFromOrderUseCase {

    @Inject
    InvoiceRepository repository;

    @Inject
    ProcessedEventRepository processed;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public void execute(InvoiceKind kind, String originType, String originId, String originNumber, String partnerId,
                        String partnerName, String currency, List<InvoiceLine> lines) {
        if (!processed.firstTime(originType + ".confirmed:" + originId)
                || repository.existsByOrigin(kind, originType, originId)) {
            return;
        }
        InvoiceCommand command = new InvoiceCommand(partnerId, partnerName, currency, null, null, originNumber, null,
                lines).validated(kind);
        Invoice invoice = Invoice.newDraft(kind, command);
        invoice.setOrigin(originType, originId, originNumber);
        repository.insert(invoice);
        events.fire(new DomainEvent(InvoiceEvents.CREATED, InvoiceEvents.InvoicePayload.of(invoice)));
    }
}
