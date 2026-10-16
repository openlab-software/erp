package software.openlab.accounting.domain.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import software.openlab.accounting.domain.shared.PageResult;

public interface InvoiceRepository {

    Optional<Invoice> findById(InvoiceId invoiceId);

    boolean existsByOrigin(InvoiceKind kind, String originType, String originId);

    PageResult<Invoice> find(InvoiceFilter filter, int page, int pageSize);

    /** Next sequence value for the kind's numbering (separate sequences for invoices and bills). */
    String nextNumber(InvoiceKind kind);

    void insert(Invoice invoice);

    void update(Invoice invoice);

    void deleteById(InvoiceId invoiceId);

    /**
     * Receivable/payable position of one kind: posted invoices still to be paid, how much of that is overdue
     * on {@code today}, and the drafts waiting to be posted.
     */
    Position position(InvoiceKind kind, LocalDate today);

    record Position(InvoiceKind kind, long openCount, BigDecimal openAmount, long overdueCount,
                    BigDecimal overdueAmount, long draftCount) {
    }
}
