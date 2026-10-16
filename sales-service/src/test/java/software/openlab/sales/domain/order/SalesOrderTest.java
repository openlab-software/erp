package software.openlab.sales.domain.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import software.openlab.sales.domain.shared.BadRequestException;
import software.openlab.sales.domain.shared.ConflictException;
import software.openlab.sales.domain.shared.Ulid;

class SalesOrderTest {

    private static final String CUSTOMER = "customer_" + Ulid.generate();
    private static final String PEN = "prod_" + Ulid.generate();
    private static final String INK = "prod_" + Ulid.generate();
    private static final String SERVICE_ID = "prod_" + Ulid.generate();

    private static OrderLine line(String product, String qty, String price, String discount, String tax) {
        return new OrderLine(product, null, "item", new BigDecimal(qty), new BigDecimal(price),
                new BigDecimal(discount), new BigDecimal(tax), null, null);
    }

    private static OrderLine service(String price) {
        return new OrderLine(SERVICE_ID, null, "Frete", BigDecimal.ONE, new BigDecimal(price), null, null, null,
                "SERVICE");
    }

    private static SalesOrder order(OrderLine... lines) {
        OrderCommand command = new OrderCommand(CUSTOMER, "ACME", null, null, null, null, null, null,
                List.of(lines)).validated();
        return SalesOrder.newQuotation("S00001", command);
    }

    @Test
    void totalsApplyDiscountThenTaxPerLine() {
        // 3 x 10.00 = 30.00, 10% off = 27.00, +5% tax = 1.35
        SalesOrder order = order(line(PEN, "3", "10", "10", "5"), line(INK, "1", "5.50", "0", "0"));

        assertEquals(new BigDecimal("32.50"), order.untaxedAmount());
        assertEquals(new BigDecimal("1.35"), order.taxAmount());
        assertEquals(new BigDecimal("33.85"), order.totalAmount());
    }

    @Test
    void confirmNeedsLinesAndFreezesTheOrder() {
        SalesOrder empty = order();
        assertThrows(ConflictException.class, empty::confirm);

        SalesOrder order = order(line(PEN, "1", "10", "0", "0"));
        order.confirm();

        assertEquals(OrderState.CONFIRMED, order.getState());
        assertEquals(DeliveryStatus.PENDING, order.getDeliveryStatus());
        assertEquals(InvoiceStatus.TO_INVOICE, order.getInvoiceStatus());
        assertThrows(ConflictException.class,
                () -> order.apply(new OrderCommand(CUSTOMER, "ACME", null, "BRL", null, null, null, null,
                        List.of())));
    }

    @Test
    void anExpiredQuotationCannotBeConfirmed() {
        OrderCommand command = new OrderCommand(CUSTOMER, "ACME", null, null, LocalDate.now().minusDays(1), null,
                null, null, List.of(line(PEN, "1", "10", "0", "0"))).validated();
        SalesOrder order = SalesOrder.newQuotation("S00002", command);

        assertThrows(ConflictException.class, order::confirm);
    }

    @Test
    void deliveriesMoveTheStatusFromPendingToPartialToDone() {
        SalesOrder order = order(line(PEN, "10", "1", "0", "0"), line(INK, "2", "1", "0", "0"));
        order.confirm();

        order.applyDelivery(Map.of(PEN, new BigDecimal("4")));
        assertEquals(DeliveryStatus.PARTIAL, order.getDeliveryStatus());

        order.applyDelivery(Map.of(PEN, new BigDecimal("6"), INK, new BigDecimal("2")));
        assertEquals(DeliveryStatus.DONE, order.getDeliveryStatus());
        assertEquals(new BigDecimal("10.000"), order.getLines().get(0).deliveredQuantity());
    }

    @Test
    void deliveriesBeyondTheOrderedQuantityAreNotCounted() {
        SalesOrder order = order(line(PEN, "2", "1", "0", "0"));
        order.confirm();

        order.applyDelivery(Map.of(PEN, new BigDecimal("5")));

        assertEquals(new BigDecimal("2.000"), order.getLines().get(0).deliveredQuantity());
        assertEquals(DeliveryStatus.DONE, order.getDeliveryStatus());
    }

    @Test
    void aDeliveredOrCancelledOrderCannotBeCancelled() {
        SalesOrder order = order(line(PEN, "2", "1", "0", "0"));
        order.confirm();
        order.applyDelivery(Map.of(PEN, BigDecimal.ONE));
        assertThrows(ConflictException.class, order::cancel);

        SalesOrder draft = order(line(PEN, "2", "1", "0", "0"));
        draft.cancel();
        assertEquals(OrderState.CANCELLED, draft.getState());
        assertThrows(ConflictException.class, draft::cancel);

        draft.resetToDraft();
        assertEquals(OrderState.DRAFT, draft.getState());
    }

    @Test
    void serviceLinesNeverWaitForADelivery() {
        SalesOrder mixed = order(line(PEN, "2", "1", "0", "0"), service("15"));
        mixed.confirm();
        mixed.applyDelivery(Map.of(PEN, new BigDecimal("2")));
        assertEquals(DeliveryStatus.DONE, mixed.getDeliveryStatus());

        SalesOrder onlyServices = order(service("15"));
        onlyServices.confirm();
        assertEquals(DeliveryStatus.NONE, onlyServices.getDeliveryStatus());
        assertEquals(InvoiceStatus.TO_INVOICE, onlyServices.getInvoiceStatus());
    }

    @Test
    void lineAndReferenceValidation() {
        assertThrows(BadRequestException.class, () -> order(new OrderLine(PEN, null, "x", BigDecimal.ONE,
                BigDecimal.ONE, null, null, null, "MAGIC")));
        assertThrows(BadRequestException.class, () -> order(line(PEN, "0", "1", "0", "0")));
        assertThrows(BadRequestException.class, () -> order(line(PEN, "1", "1", "101", "0")));
        assertThrows(BadRequestException.class, () -> order(line("product_" + Ulid.generate(), "1", "1", "0", "0")));
        assertThrows(BadRequestException.class, () -> new OrderCommand("supplier_" + Ulid.generate(), "ACME", null,
                null, null, null, null, null, List.of()).validated());
    }
}
