package software.openlab.purchase.domain.order;

import java.time.LocalDate;
import java.util.List;
import software.openlab.purchase.domain.shared.BadRequestException;
import software.openlab.purchase.domain.shared.Fields;
import software.openlab.purchase.domain.shared.Refs;

/** Editable data of an order (RFQ), as sent by a client. */
public record OrderCommand(
        String supplierId,
        String supplierName,
        String origin,
        String currency,
        LocalDate expectedDate,
        String shipToAddress,
        String buyer,
        String note,
        List<OrderLine> lines) {

    public static final int MAX_LINES = 200;

    public OrderCommand validated() {
        String supplier = Refs.require("supplier", supplierId);
        String name = Fields.requireNonBlank(supplierName, "supplier_name");
        String cur = currency == null || currency.isBlank() ? "BRL" : currency.trim().toUpperCase();
        if (!cur.matches("[A-Z]{3}")) {
            throw new BadRequestException("error.field.invalid", "currency", currency);
        }
        List<OrderLine> clean = (lines == null ? List.<OrderLine>of() : lines).stream()
                .map(OrderLine::validated).toList();
        if (clean.size() > MAX_LINES) {
            throw new BadRequestException("error.order.line.tooMany", MAX_LINES);
        }
        return new OrderCommand(supplier, name, blankToNull(origin), cur, expectedDate,
                blankToNull(shipToAddress), blankToNull(buyer), blankToNull(note), clean);
    }

    private static String blankToNull(String v) {
        return v == null || v.isBlank() ? null : v.trim();
    }
}
