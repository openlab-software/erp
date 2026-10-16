package software.openlab.accounting.application.usecase.invoice;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDate;
import java.util.List;
import software.openlab.accounting.domain.invoice.InvoiceKind;
import software.openlab.accounting.domain.invoice.InvoiceRepository;
import software.openlab.accounting.domain.invoice.InvoiceRepository.Position;

/** Accounts receivable and payable at a glance: open, overdue and draft documents. */
@ApplicationScoped
public class GetAccountingPositionUseCase {

    @Inject
    InvoiceRepository repository;

    public List<Position> execute() {
        LocalDate today = LocalDate.now();
        return List.of(repository.position(InvoiceKind.CUSTOMER_INVOICE, today),
                repository.position(InvoiceKind.VENDOR_BILL, today));
    }
}
