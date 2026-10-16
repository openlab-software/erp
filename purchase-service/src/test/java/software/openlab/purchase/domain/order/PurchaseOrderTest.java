package software.openlab.purchase.domain.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import software.openlab.purchase.domain.shared.BadRequestException;
import software.openlab.purchase.domain.shared.ConflictException;
import software.openlab.purchase.domain.shared.Ulid;

class PurchaseOrderTest {

    private static final String SUPPLIER = "supplier_" + Ulid.generate();
    private static final String PEN = "prod_" + Ulid.generate();

    private static PurchaseOrder order(BigDecimal qty) {
        OrderLine line = new OrderLine(PEN, null, "caneta", qty, new BigDecimal("2.50"), null, null, null, null);
        return PurchaseOrder.newRFQ("P00001", new OrderCommand(SUPPLIER, "Papelaria", null, null, null, null, null,
                null, List.of(line)).validated());
    }

    @Test
    void confirmingStartsThePendingReceiptAndBillingTracks() {
        PurchaseOrder order = order(new BigDecimal("10"));
        order.confirm();

        assertEquals(OrderState.CONFIRMED, order.getState());
        assertEquals(ReceiptStatus.PENDING, order.getReceiptStatus());
        assertEquals(BillStatus.TO_BILL, order.getBillStatus());
        assertEquals(new BigDecimal("25.00"), order.totalAmount());
    }

    @Test
    void receiptsAccumulateUntilTheOrderIsComplete() {
        PurchaseOrder order = order(new BigDecimal("10"));
        order.confirm();

        order.applyReceipt(Map.of(PEN, new BigDecimal("3")));
        assertEquals(ReceiptStatus.PARTIAL, order.getReceiptStatus());

        order.applyReceipt(Map.of(PEN, new BigDecimal("7")));
        assertEquals(ReceiptStatus.DONE, order.getReceiptStatus());

        order.markBilled();
        assertEquals(BillStatus.BILLED, order.getBillStatus());
        assertThrows(ConflictException.class, order::cancel);
    }

    @Test
    void theSupplierMustBeASupplierReference() {
        assertThrows(BadRequestException.class, () -> new OrderCommand("customer_" + Ulid.generate(), "x", null,
                null, null, null, null, null, List.of()).validated());
    }
}
