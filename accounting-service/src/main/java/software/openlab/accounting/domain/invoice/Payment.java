package software.openlab.accounting.domain.invoice;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** A payment registered against an invoice (received for a customer invoice, made for a vendor bill). */
public record Payment(PaymentId paymentId, BigDecimal amount, LocalDate paidOn, PaymentMethod method,
                      String reference, Instant createdAt) {
}
