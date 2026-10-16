package software.openlab.accounting.application.usecase.invoice;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDate;
import software.openlab.accounting.domain.invoice.Invoice;
import software.openlab.accounting.domain.invoice.InvoiceFilter;
import software.openlab.accounting.domain.invoice.InvoiceKind;
import software.openlab.accounting.domain.invoice.InvoiceRepository;
import software.openlab.accounting.domain.invoice.InvoiceState;
import software.openlab.accounting.domain.invoice.PaymentState;
import software.openlab.accounting.domain.shared.PageResult;

@ApplicationScoped
public class ListInvoicesUseCase {

    @Inject
    InvoiceRepository repository;

    /** {@code overdue=true} keeps only posted, unpaid invoices past their due date. */
    public PageResult<Invoice> execute(String q, String kind, String state, String paymentState, String partnerId,
                                       Boolean overdue, Integer page, Integer pageSize) {
        InvoiceFilter filter = new InvoiceFilter(
                q,
                isBlank(kind) ? null : InvoiceKind.parse(kind),
                isBlank(state) ? null : InvoiceState.parse(state),
                isBlank(paymentState) ? null : PaymentState.parse(paymentState),
                isBlank(partnerId) ? null : partnerId.trim(),
                Boolean.TRUE.equals(overdue) ? LocalDate.now() : null);
        return repository.find(filter, PageResult.normalizePage(page), PageResult.normalizePageSize(pageSize));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
