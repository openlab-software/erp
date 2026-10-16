package software.openlab.accounting.domain.invoice;

import software.openlab.accounting.domain.shared.BadRequestException;

/** CUSTOMER_INVOICE = receivable (sold to a customer); VENDOR_BILL = payable (bought from a supplier). */
public enum InvoiceKind {
    CUSTOMER_INVOICE("customer", "INV"),
    VENDOR_BILL("supplier", "BILL");

    private final String partnerPrefix;
    private final String numberPrefix;

    InvoiceKind(String partnerPrefix, String numberPrefix) {
        this.partnerPrefix = partnerPrefix;
        this.numberPrefix = numberPrefix;
    }

    /** Prefix of the partner's public id: customer_* for invoices, supplier_* for bills. */
    public String partnerPrefix() {
        return partnerPrefix;
    }

    public String numberPrefix() {
        return numberPrefix;
    }

    public static InvoiceKind parse(String raw) {
        try {
            return valueOf(raw.trim().toUpperCase());
        } catch (RuntimeException e) {
            throw new BadRequestException("error.field.invalid", "kind", raw);
        }
    }
}
