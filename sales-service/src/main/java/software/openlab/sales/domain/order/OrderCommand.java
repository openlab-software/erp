package software.openlab.sales.domain.order;

import java.time.LocalDate;
import java.util.List;
import software.openlab.sales.domain.shared.BadRequestException;
import software.openlab.sales.domain.shared.Fields;
import software.openlab.sales.domain.shared.Refs;

/** Editable data of an order (quotation), as sent by a client. */
public record OrderCommand(
        String customerId,
        String customerName,
        String leadId,
        String currency,
        LocalDate validUntil,
        String deliveryAddress,
        String salesperson,
        String note,
        List<OrderLine> lines) {

    public static final int MAX_LINES = 200;

    public OrderCommand validated() {
        String customer = Refs.require("customer", customerId);
        String name = Fields.requireNonBlank(customerName, "customer_name");
        String cur = currency == null || currency.isBlank() ? "BRL" : currency.trim().toUpperCase();
        if (!cur.matches("[A-Z]{3}")) {
            throw new BadRequestException("error.field.invalid", "currency", currency);
        }
        List<OrderLine> clean = (lines == null ? List.<OrderLine>of() : lines).stream()
                .map(OrderLine::validated).toList();
        if (clean.size() > MAX_LINES) {
            throw new BadRequestException("error.order.line.tooMany", MAX_LINES);
        }
        return new OrderCommand(customer, name, Refs.optional("lead", leadId), cur, validUntil,
                blankToNull(deliveryAddress), blankToNull(salesperson), blankToNull(note), clean);
    }

    private static String blankToNull(String v) {
        return v == null || v.isBlank() ? null : v.trim();
    }
}
