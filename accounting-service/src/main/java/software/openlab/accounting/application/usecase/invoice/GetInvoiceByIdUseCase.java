package software.openlab.accounting.application.usecase.invoice;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.openlab.accounting.domain.invoice.Invoice;
import software.openlab.accounting.domain.invoice.InvoiceId;
import software.openlab.accounting.domain.invoice.InvoiceRepository;
import software.openlab.accounting.domain.shared.NotFoundException;

@ApplicationScoped
public class GetInvoiceByIdUseCase {

    @Inject
    InvoiceRepository repository;

    public Invoice execute(InvoiceId invoiceId) {
        return repository.findById(invoiceId)
                .orElseThrow(() -> new NotFoundException("error.invoice.notFound", invoiceId));
    }
}
