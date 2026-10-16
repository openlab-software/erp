package software.openlab.sales.domain.order;

import java.math.BigDecimal;
import java.util.List;

public final class OrderEvents {

    public static final String CREATED = "sales_order.created";
    public static final String UPDATED = "sales_order.updated";
    public static final String CONFIRMED = "sales_order.confirmed";
    public static final String CANCELLED = "sales_order.cancelled";
    public static final String DELETED = "sales_order.deleted";

    private OrderEvents() {
    }

    /** Summary used by created/updated/cancelled. */
    public record OrderPayload(String id, String number, String state, String customerId, String customerName,
                               String currency, BigDecimal totalAmount) {
        public static OrderPayload of(SalesOrder o) {
            return new OrderPayload(o.getOrderId().toString(), o.getNumber(), o.getState().name(), o.getCustomerId(),
                    o.getCustomerName(), o.getCurrency(), o.totalAmount());
        }
    }

    /**
     * Everything the modules that integrate with sales (inventory: create a delivery; accounting: create the
     * invoice) need to act without calling back.
     */
    public record OrderConfirmedPayload(String id, String number, String customerId, String customerName,
                                        String deliveryAddress, String currency, BigDecimal untaxedAmount,
                                        BigDecimal taxAmount, BigDecimal totalAmount, List<Line> lines) {
        public static OrderConfirmedPayload of(SalesOrder o) {
            return new OrderConfirmedPayload(o.getOrderId().toString(), o.getNumber(), o.getCustomerId(),
                    o.getCustomerName(), o.getDeliveryAddress(), o.getCurrency(), o.untaxedAmount(), o.taxAmount(),
                    o.totalAmount(), o.getLines().stream().map(Line::of).toList());
        }
    }

    public record Line(String productId, String sku, String description, BigDecimal quantity, BigDecimal unitPrice,
                       BigDecimal discountPercent, BigDecimal taxPercent, String productType) {
        static Line of(OrderLine l) {
            return new Line(l.productId(), l.sku(), l.description(), l.quantity(), l.unitPrice(),
                    l.discountPercent(), l.taxPercent(), l.productType());
        }
    }

    public record OrderDeletedPayload(String id) {
    }
}
