package software.openlab.accounting.domain.invoice;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.openlab.accounting.domain.shared.BadRequestException;
import software.openlab.accounting.domain.shared.ConflictException;

/**
 * A customer invoice (receivable) or vendor bill (payable). It is a DRAFT until posted; posting assigns the
 * number and fixes the due date, after which the only changes are payments or cancelling it. The payment state
 * always follows the registered payments.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {

    public static final int DEFAULT_TERM_DAYS = 30;

    private InvoiceId invoiceId;
    private InvoiceKind kind;
    /** Null while DRAFT; assigned when posted (INV/00001, BILL/00001). */
    private String number;
    private InvoiceState state;
    private PaymentState paymentState;
    private String partnerId;
    private String partnerName;
    private String currency;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    /** Customer payment reference or the supplier's own invoice number. */
    private String reference;
    private String note;
    /** Document that originated it (SALES_ORDER / PURCHASE_ORDER), when created by another module. */
    private String originType;
    private String originId;
    private String originNumber;
    private List<InvoiceLine> lines;
    private List<Payment> payments;
    private Instant postedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public static Invoice newDraft(InvoiceKind kind, InvoiceCommand command) {
        Invoice invoice = new Invoice();
        invoice.invoiceId = InvoiceId.generate();
        invoice.kind = kind;
        invoice.state = InvoiceState.DRAFT;
        invoice.paymentState = PaymentState.NOT_PAID;
        invoice.payments = new ArrayList<>();
        invoice.createdAt = Instant.now();
        invoice.apply(command);
        return invoice;
    }

    public void setOrigin(String type, String id, String number) {
        this.originType = type;
        this.originId = id;
        this.originNumber = number;
    }

    /** Replaces every editable field and all lines (PUT semantics); only drafts are editable. */
    public void apply(InvoiceCommand c) {
        if (state != InvoiceState.DRAFT) {
            throw new ConflictException("error.invoice.notDraft", state);
        }
        this.partnerId = c.partnerId();
        this.partnerName = c.partnerName();
        this.currency = c.currency();
        this.invoiceDate = c.invoiceDate();
        this.dueDate = c.dueDate();
        this.reference = c.reference();
        this.note = c.note();
        this.lines = new ArrayList<>(c.lines());
    }

    public BigDecimal untaxedAmount() {
        return lines.stream().map(InvoiceLine::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal taxAmount() {
        return lines.stream().map(InvoiceLine::taxAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal totalAmount() {
        return untaxedAmount().add(taxAmount());
    }

    public BigDecimal paidAmount() {
        return payments.stream().map(Payment::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** What is still to be paid. */
    public BigDecimal residualAmount() {
        return totalAmount().subtract(paidAmount()).max(BigDecimal.ZERO);
    }

    public boolean isOverdue(LocalDate today) {
        return state == InvoiceState.POSTED && paymentState != PaymentState.PAID && dueDate != null
                && dueDate.isBefore(today);
    }

    /** Validates and freezes the invoice; the caller supplies the next sequence number. */
    public void post(String assignedNumber) {
        if (state != InvoiceState.DRAFT) {
            throw new ConflictException("error.invoice.notDraft", state);
        }
        if (lines.isEmpty()) {
            throw new ConflictException("error.invoice.empty");
        }
        if (totalAmount().signum() <= 0) {
            throw new ConflictException("error.invoice.zeroTotal");
        }
        if (invoiceDate == null) {
            invoiceDate = LocalDate.now();
        }
        if (dueDate == null) {
            dueDate = invoiceDate.plusDays(DEFAULT_TERM_DAYS);
        }
        number = assignedNumber;
        state = InvoiceState.POSTED;
        postedAt = Instant.now();
    }

    /** A draft or an unpaid posted invoice can be cancelled. */
    public void cancel() {
        if (state == InvoiceState.CANCELLED) {
            throw new ConflictException("error.invoice.alreadyCancelled");
        }
        if (!payments.isEmpty()) {
            throw new ConflictException("error.invoice.hasPayments");
        }
        state = InvoiceState.CANCELLED;
    }

    /** CANCELLED back to DRAFT. The number assigned at posting is kept (fiscal sequences have no holes). */
    public void resetToDraft() {
        if (state != InvoiceState.CANCELLED) {
            throw new ConflictException("error.invoice.notCancelled", state);
        }
        state = InvoiceState.DRAFT;
        postedAt = null;
    }

    /** Registers a payment; the amount cannot exceed what is still due. Returns the new payment. */
    public Payment registerPayment(BigDecimal amount, LocalDate paidOn, PaymentMethod method, String reference) {
        if (state != InvoiceState.POSTED) {
            throw new ConflictException("error.invoice.notPosted", state);
        }
        if (amount == null || amount.signum() <= 0) {
            throw new BadRequestException("error.payment.amount");
        }
        BigDecimal value = amount.setScale(2, java.math.RoundingMode.HALF_UP);
        if (value.compareTo(residualAmount()) > 0) {
            throw new ConflictException("error.payment.exceedsResidual", residualAmount());
        }
        Payment payment = new Payment(PaymentId.generate(), value, paidOn == null ? LocalDate.now() : paidOn,
                method == null ? PaymentMethod.OTHER : method,
                reference == null || reference.isBlank() ? null : reference.trim(), Instant.now());
        payments.add(payment);
        refreshPaymentState();
        return payment;
    }

    private void refreshPaymentState() {
        BigDecimal paid = paidAmount();
        if (paid.signum() == 0) {
            paymentState = PaymentState.NOT_PAID;
        } else if (paid.compareTo(totalAmount()) >= 0) {
            paymentState = PaymentState.PAID;
        } else {
            paymentState = PaymentState.PARTIAL;
        }
    }
}
