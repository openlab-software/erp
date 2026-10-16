package software.openlab.accounting.domain.invoice;

import java.time.LocalDate;

/**
 * Listing filters; every field may be null. q matches the number, the reference or the partner name;
 * overdueOn keeps posted, unpaid invoices due before that date.
 */
public record InvoiceFilter(String q, InvoiceKind kind, InvoiceState state, PaymentState paymentState,
                            String partnerId, LocalDate overdueOn) {
}
