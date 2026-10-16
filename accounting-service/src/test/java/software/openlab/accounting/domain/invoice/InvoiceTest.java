package software.openlab.accounting.domain.invoice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import software.openlab.accounting.domain.shared.BadRequestException;
import software.openlab.accounting.domain.shared.ConflictException;
import software.openlab.accounting.domain.shared.Ulid;

class InvoiceTest {

    private static final String CUSTOMER = "customer_" + Ulid.generate();

    private static Invoice invoice(String price) {
        InvoiceLine line = new InvoiceLine(null, "Servico", BigDecimal.ONE, new BigDecimal(price), null, null);
        InvoiceCommand command = new InvoiceCommand(CUSTOMER, "ACME", null, null, null, null, null, List.of(line))
                .validated(InvoiceKind.CUSTOMER_INVOICE);
        return Invoice.newDraft(InvoiceKind.CUSTOMER_INVOICE, command);
    }

    @Test
    void postingNumbersTheInvoiceDefaultsTheDueDateAndFreezesIt() {
        Invoice invoice = invoice("100");
        assertNull(invoice.getNumber());

        invoice.post("INV/00001");

        assertEquals(InvoiceState.POSTED, invoice.getState());
        assertEquals("INV/00001", invoice.getNumber());
        assertEquals(LocalDate.now().plusDays(Invoice.DEFAULT_TERM_DAYS), invoice.getDueDate());
        assertThrows(ConflictException.class, () -> invoice.post("INV/00002"));
        assertThrows(ConflictException.class, () -> invoice.apply(new InvoiceCommand(CUSTOMER, "ACME", "BRL", null,
                null, null, null, List.of())));
    }

    @Test
    void cannotPostAnEmptyOrZeroInvoice() {
        Invoice empty = Invoice.newDraft(InvoiceKind.CUSTOMER_INVOICE,
                new InvoiceCommand(CUSTOMER, "ACME", null, null, null, null, null, List.of())
                        .validated(InvoiceKind.CUSTOMER_INVOICE));
        assertThrows(ConflictException.class, () -> empty.post("INV/1"));
        assertThrows(ConflictException.class, () -> invoice("0").post("INV/2"));
    }

    @Test
    void paymentsFollowTheResidualAndSettleTheInvoice() {
        Invoice invoice = invoice("100");
        assertThrows(ConflictException.class,
                () -> invoice.registerPayment(BigDecimal.TEN, null, PaymentMethod.PIX, null));

        invoice.post("INV/00001");
        invoice.registerPayment(new BigDecimal("40"), null, PaymentMethod.PIX, "pix-1");
        assertEquals(PaymentState.PARTIAL, invoice.getPaymentState());
        assertEquals(new BigDecimal("60.00"), invoice.residualAmount());

        assertThrows(ConflictException.class,
                () -> invoice.registerPayment(new BigDecimal("60.01"), null, PaymentMethod.CASH, null));
        assertThrows(BadRequestException.class, () -> invoice.registerPayment(BigDecimal.ZERO, null, null, null));

        invoice.registerPayment(new BigDecimal("60"), null, null, null);
        assertEquals(PaymentState.PAID, invoice.getPaymentState());
        assertEquals(BigDecimal.ZERO.setScale(2), invoice.residualAmount());
    }

    @Test
    void aPaidInvoiceCannotBeCancelledButAnUnpaidOneKeepsItsNumberOnReset() {
        Invoice paid = invoice("10");
        paid.post("INV/00001");
        paid.registerPayment(BigDecimal.TEN, null, null, null);
        assertThrows(ConflictException.class, paid::cancel);

        Invoice unpaid = invoice("10");
        unpaid.post("INV/00002");
        unpaid.cancel();
        unpaid.resetToDraft();

        assertEquals(InvoiceState.DRAFT, unpaid.getState());
        assertEquals("INV/00002", unpaid.getNumber());
    }

    @Test
    void overdueMeansPostedUnpaidAndPastDue() {
        Invoice invoice = invoice("10");
        invoice.setDueDate(LocalDate.now().minusDays(1));
        invoice.post("INV/00003");

        assertTrue(invoice.isOverdue(LocalDate.now()));
        invoice.registerPayment(BigDecimal.TEN, null, null, null);
        assertEquals(false, invoice.isOverdue(LocalDate.now()));
    }

    @Test
    void partnerMustMatchTheKind() {
        InvoiceCommand command = new InvoiceCommand("supplier_" + Ulid.generate(), "x", null, null, null, null, null,
                List.of());
        assertThrows(BadRequestException.class, () -> command.validated(InvoiceKind.CUSTOMER_INVOICE));
        command.validated(InvoiceKind.VENDOR_BILL);
    }
}
