package software.openlab.accounting.domain.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class InvoiceEvents {

    public static final String CREATED = "invoice.created";
    public static final String UPDATED = "invoice.updated";
    public static final String POSTED = "invoice.posted";
    public static final String CANCELLED = "invoice.cancelled";
    public static final String PAID = "invoice.paid";
    public static final String DELETED = "invoice.deleted";
    public static final String PAYMENT_REGISTERED = "payment.registered";

    private InvoiceEvents() {
    }

    /**
     * Summary for every invoice event. {@code originType/originId} let the module that originated it (sales,
     * purchase) mark its own order as invoiced when it sees {@code invoice.posted}.
     */
    public record InvoicePayload(String id, String number, String kind, String state, String paymentState,
                                 String partnerId, String partnerName, String originType, String originId,
                                 BigDecimal totalAmount, BigDecimal residualAmount, LocalDate dueDate) {
        public static InvoicePayload of(Invoice i) {
            return new InvoicePayload(i.getInvoiceId().toString(), i.getNumber(), i.getKind().name(),
                    i.getState().name(), i.getPaymentState().name(), i.getPartnerId(), i.getPartnerName(),
                    i.getOriginType(), i.getOriginId(), i.totalAmount(), i.residualAmount(), i.getDueDate());
        }
    }

    public record PaymentPayload(String id, String invoiceId, String kind, BigDecimal amount, LocalDate paidOn,
                                 String method) {
        public static PaymentPayload of(Invoice i, Payment p) {
            return new PaymentPayload(p.paymentId().toString(), i.getInvoiceId().toString(), i.getKind().name(),
                    p.amount(), p.paidOn(), p.method().name());
        }
    }

    public record InvoiceDeletedPayload(String id) {
    }
}
