package software.openlab.accounting.application.usecase.invoice;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import software.openlab.accounting.domain.invoice.Invoice;
import software.openlab.accounting.domain.invoice.InvoiceEvents;
import software.openlab.accounting.domain.invoice.InvoiceId;
import software.openlab.accounting.domain.invoice.InvoiceRepository;
import software.openlab.accounting.domain.invoice.Payment;
import software.openlab.accounting.domain.invoice.PaymentMethod;
import software.openlab.accounting.domain.invoice.PaymentState;
import software.openlab.accounting.domain.shared.DomainEvent;

/** Registers a payment received (customer invoice) or made (vendor bill). Settling the invoice fires invoice.paid. */
@ApplicationScoped
public class RegisterPaymentUseCase {

    @Inject
    InvoiceRepository repository;

    @Inject
    GetInvoiceByIdUseCase getInvoiceById;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public Invoice execute(InvoiceId invoiceId, BigDecimal amount, LocalDate paidOn, String method,
                           String reference) {
        Invoice invoice = getInvoiceById.execute(invoiceId);
        Payment payment = invoice.registerPayment(amount, paidOn, PaymentMethod.parse(method), reference);
        repository.update(invoice);

        events.fire(new DomainEvent(InvoiceEvents.PAYMENT_REGISTERED, InvoiceEvents.PaymentPayload.of(invoice, payment)));
        if (invoice.getPaymentState() == PaymentState.PAID) {
            events.fire(new DomainEvent(InvoiceEvents.PAID, InvoiceEvents.InvoicePayload.of(invoice)));
        }
        return getInvoiceById.execute(invoiceId);
    }
}
