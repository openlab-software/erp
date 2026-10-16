package software.openlab.purchase.domain.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import software.openlab.purchase.domain.shared.BadRequestException;
import software.openlab.purchase.domain.shared.Fields;
import software.openlab.purchase.domain.shared.Refs;

/**
 * One line of an order. {@code sku} and {@code description} are snapshots of the catalog product at the time
 * the line was written, so the order keeps reading the same after the product is renamed.
 */
public record OrderLine(
        String productId,
        String sku,
        String description,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal discountPercent,
        BigDecimal taxPercent,
        BigDecimal receivedQuantity,
        String productType) {

    public static final String GOOD = "GOOD";
    public static final String SERVICE = "SERVICE";

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    /** Normalizes and validates a line coming from a request. */
    public static OrderLine validated(OrderLine raw) {
        String productId = Refs.require("prod", raw.productId());
        String description = Fields.requireNonBlank(raw.description(), "description");
        BigDecimal quantity = raw.quantity() == null ? BigDecimal.ZERO : raw.quantity();
        if (quantity.signum() <= 0) {
            throw new BadRequestException("error.order.line.quantity");
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
        String sku = raw.sku() == null || raw.sku().isBlank() ? null : raw.sku().trim();
        String type = raw.productType() == null || raw.productType().isBlank()
                ? GOOD : raw.productType().trim().toUpperCase();
        if (!GOOD.equals(type) && !SERVICE.equals(type)) {
            throw new BadRequestException("error.field.invalid", "product_type", raw.productType());
        }
        return new OrderLine(productId, sku, description, quantity.setScale(3, RoundingMode.HALF_UP),
                price.setScale(2, RoundingMode.HALF_UP), discount.setScale(2, RoundingMode.HALF_UP),
                tax.setScale(2, RoundingMode.HALF_UP), BigDecimal.ZERO.setScale(3), type);
    }

    /** quantity x unit price, less the discount. */
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

    /** Only goods are received; a service line never waits for a receipt. */
    public boolean isGoods() {
        return !SERVICE.equals(productType);
    }

    public boolean isFullyReceived() {
        return receivedQuantity.compareTo(quantity) >= 0;
    }

    public OrderLine withReceived(BigDecimal received) {
        return new OrderLine(productId, sku, description, quantity, unitPrice, discountPercent, taxPercent,
                received.setScale(3, RoundingMode.HALF_UP), productType);
    }
}
