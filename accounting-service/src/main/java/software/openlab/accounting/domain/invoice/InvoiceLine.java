package software.openlab.accounting.domain.invoice;

import java.math.BigDecimal;
import java.math.RoundingMode;
import software.openlab.accounting.domain.shared.BadRequestException;
import software.openlab.accounting.domain.shared.Fields;
import software.openlab.accounting.domain.shared.Refs;

/** One invoice line. {@code productId} is optional: services and fees have no catalog product. */
public record InvoiceLine(
        String productId,
        String description,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal discountPercent,
        BigDecimal taxPercent) {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    public static InvoiceLine validated(InvoiceLine raw) {
        String description = Fields.requireNonBlank(raw.description(), "description");
        BigDecimal quantity = raw.quantity() == null ? BigDecimal.ONE : raw.quantity();
        if (quantity.signum() <= 0) {
            throw new BadRequestException("error.invoice.line.quantity");
        }
        BigDecimal price = raw.unitPrice() == null ? BigDecimal.ZERO : raw.unitPrice();
        if (price.signum() < 0) {
            throw new BadRequestException("error.field.invalid", "unit_price", raw.unitPrice());
        }
        BigDecimal discount = raw.discountPercent() == null ? BigDecimal.ZERO : raw.discountPercent();
        if (discount.signum() < 0 || discount.compareTo(HUNDRED) > 0) {
            throw new BadRequestException("error.field.invalid", "discount_percent", raw.discountPercent());
        }
        BigDecimal tax = raw.taxPercent() == null ? BigDecimal.ZERO : raw.taxPercent();
        if (tax.signum() < 0 || tax.compareTo(HUNDRED) > 0) {
            throw new BadRequestException("error.field.invalid", "tax_percent", raw.taxPercent());
        }
        return new InvoiceLine(Refs.optional("prod", raw.productId()), description,
                quantity.setScale(3, RoundingMode.HALF_UP), price.setScale(2, RoundingMode.HALF_UP),
                discount.setScale(2, RoundingMode.HALF_UP), tax.setScale(2, RoundingMode.HALF_UP));
    }

    public BigDecimal subtotal() {
        BigDecimal gross = quantity.multiply(unitPrice);
        BigDecimal discount = gross.multiply(discountPercent).divide(HUNDRED, 2, RoundingMode.HALF_UP);
        return gross.subtract(discount).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal taxAmount() {
        return subtotal().multiply(taxPercent).divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }

    public BigDecimal total() {
        return subtotal().add(taxAmount());
    }
}
