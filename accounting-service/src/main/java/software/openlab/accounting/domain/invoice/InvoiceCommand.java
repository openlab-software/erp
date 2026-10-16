package software.openlab.accounting.domain.invoice;

import java.time.LocalDate;
import java.util.List;
import software.openlab.accounting.domain.shared.BadRequestException;
import software.openlab.accounting.domain.shared.Fields;
import software.openlab.accounting.domain.shared.Refs;

/** Editable data of an invoice. {@code partnerId} is a customer_* id for invoices and a supplier_* id for bills. */
public record InvoiceCommand(
        String partnerId,
        String partnerName,
        String currency,
        LocalDate invoiceDate,
        LocalDate dueDate,
        String reference,
        String note,
        List<InvoiceLine> lines) {

    public static final int MAX_LINES = 200;

    public InvoiceCommand validated(InvoiceKind kind) {
        String partner = Refs.require(kind.partnerPrefix(), partnerId);
        String name = Fields.requireNonBlank(partnerName, "partner_name");
        String cur = currency == null || currency.isBlank() ? "BRL" : currency.trim().toUpperCase();
        if (!cur.matches("[A-Z]{3}")) {
            throw new BadRequestException("error.field.invalid", "currency", currency);
        }
        if (invoiceDate != null && dueDate != null && dueDate.isBefore(invoiceDate)) {
            throw new BadRequestException("error.invoice.dueBeforeDate");
        }
        List<InvoiceLine> clean = (lines == null ? List.<InvoiceLine>of() : lines).stream()
                .map(InvoiceLine::validated).toList();
        if (clean.size() > MAX_LINES) {
            throw new BadRequestException("error.invoice.line.tooMany", MAX_LINES);
        }
        return new InvoiceCommand(partner, name, cur, invoiceDate, dueDate, blankToNull(reference),
                blankToNull(note), clean);
    }

    private static String blankToNull(String v) {
        return v == null || v.isBlank() ? null : v.trim();
    }
}
