package software.openlab.accounting.infra.rest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import software.openlab.accounting.domain.invoice.Invoice;
import software.openlab.accounting.domain.invoice.InvoiceCommand;
import software.openlab.accounting.domain.invoice.InvoiceLine;
import software.openlab.accounting.domain.invoice.InvoiceRepository.Position;
import software.openlab.accounting.domain.invoice.Payment;
import software.openlab.accounting.domain.shared.PageResult;

public final class InvoiceDTOs {

    private InvoiceDTOs() {
    }

    public record LineRequest(
            String productId,
            @NotBlank String description,
            @Positive BigDecimal quantity,
            @PositiveOrZero BigDecimal unitPrice,
            @PositiveOrZero BigDecimal discountPercent,
            @PositiveOrZero BigDecimal taxPercent
    ) {
        InvoiceLine toDomain() {
            return new InvoiceLine(productId, description, quantity, unitPrice, discountPercent, taxPercent);
        }
    }

    /**
     * {@code kind} (CUSTOMER_INVOICE or VENDOR_BILL) is read only on creation. {@code partnerId} is a
     * customer_* id for invoices and a supplier_* id for bills.
     */
    public record InvoiceRequest(
            String kind,
            @NotBlank String partnerId,
            @NotBlank String partnerName,
            String currency,
            LocalDate invoiceDate,
            LocalDate dueDate,
            String reference,
            String note,
            @Valid @Size(max = InvoiceCommand.MAX_LINES) List<LineRequest> lines
    ) {
        public InvoiceCommand toCommand() {
            List<InvoiceLine> mapped = lines == null ? List.of() : lines.stream().map(LineRequest::toDomain).toList();
            return new InvoiceCommand(partnerId, partnerName, currency, invoiceDate, dueDate, reference, note,
                    mapped);
        }
    }

    public record PaymentRequest(@NotNull @Positive BigDecimal amount, LocalDate paidOn, String method,
                                 String reference) {
    }

    public record LineResponse(String productId, String description, BigDecimal quantity, BigDecimal unitPrice,
                               BigDecimal discountPercent, BigDecimal taxPercent, BigDecimal subtotal,
                               BigDecimal total) {
        static LineResponse from(InvoiceLine l) {
            return new LineResponse(l.productId(), l.description(), l.quantity(), l.unitPrice(),
                    l.discountPercent(), l.taxPercent(), l.subtotal(), l.total());
        }
    }

    public record PaymentResponse(String paymentId, BigDecimal amount, LocalDate paidOn, String method,
                                  String reference, Instant createdAt) {
        static PaymentResponse from(Payment p) {
            return new PaymentResponse(p.paymentId().toString(), p.amount(), p.paidOn(), p.method().name(),
                    p.reference(), p.createdAt());
        }
    }

    public record InvoiceResponse(
            String invoiceId,
            String kind,
            String number,
            String state,
            String paymentState,
            String partnerId,
            String partnerName,
            String currency,
            LocalDate invoiceDate,
            LocalDate dueDate,
            boolean overdue,
            String reference,
            String note,
            String originType,
            String originId,
            String originNumber,
            BigDecimal untaxedAmount,
            BigDecimal taxAmount,
            BigDecimal totalAmount,
            BigDecimal paidAmount,
            BigDecimal residualAmount,
            List<LineResponse> lines,
            List<PaymentResponse> payments,
            Instant postedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static InvoiceResponse from(Invoice i) {
            return new InvoiceResponse(i.getInvoiceId().toString(), i.getKind().name(), i.getNumber(),
                    i.getState().name(), i.getPaymentState().name(), i.getPartnerId(), i.getPartnerName(),
                    i.getCurrency(), i.getInvoiceDate(), i.getDueDate(), i.isOverdue(LocalDate.now()),
                    i.getReference(), i.getNote(), i.getOriginType(), i.getOriginId(), i.getOriginNumber(),
                    i.untaxedAmount(), i.taxAmount(), i.totalAmount(), i.paidAmount(), i.residualAmount(),
                    i.getLines().stream().map(LineResponse::from).toList(),
                    i.getPayments().stream().map(PaymentResponse::from).toList(), i.getPostedAt(), i.getCreatedAt(),
                    i.getUpdatedAt());
        }
    }

    public record InvoicePageResponse(List<InvoiceResponse> data, int page, int pageSize, long total) {
        public static InvoicePageResponse from(PageResult<Invoice> page) {
            return new InvoicePageResponse(page.data().stream().map(InvoiceResponse::from).toList(), page.page(),
                    page.pageSize(), page.total());
        }
    }

    public record PositionResponse(String kind, long openCount, BigDecimal openAmount, long overdueCount,
                                   BigDecimal overdueAmount, long draftCount) {
        static PositionResponse from(Position p) {
            return new PositionResponse(p.kind().name(), p.openCount(), p.openAmount(), p.overdueCount(),
                    p.overdueAmount(), p.draftCount());
        }
    }

    /** Receivable (CUSTOMER_INVOICE) and payable (VENDOR_BILL) position. */
    public record AccountingPositionResponse(List<PositionResponse> data) {
        public static AccountingPositionResponse from(List<Position> positions) {
            return new AccountingPositionResponse(positions.stream().map(PositionResponse::from).toList());
        }
    }
}
