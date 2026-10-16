package software.openlab.purchase.domain.order;

import java.math.BigDecimal;
import java.util.List;

public final class OrderEvents {

    public static final String CREATED = "purchase_order.created";
    public static final String UPDATED = "purchase_order.updated";
    public static final String CONFIRMED = "purchase_order.confirmed";
    public static final String CANCELLED = "purchase_order.cancelled";
    public static final String DELETED = "purchase_order.deleted";

    private OrderEvents() {
    }

    /** Summary used by created/updated/cancelled. */
    public record OrderPayload(String id, String number, String state, String supplierId, String supplierName,
                               String currency, BigDecimal totalAmount) {
        public static OrderPayload of(PurchaseOrder o) {
            return new OrderPayload(o.getOrderId().toString(), o.getNumber(), o.getState().name(), o.getSupplierId(),
                    o.getSupplierName(), o.getCurrency(), o.totalAmount());
        }
    }

    /**
     * Everything the modules that integrate with purchase (inventory: create a receipt; accounting: create the
     * invoice) need to act without calling back.
     */
    public record OrderConfirmedPayload(String id, String number, String supplierId, String supplierName,
                                        String shipToAddress, String currency, BigDecimal untaxedAmount,
                                        BigDecimal taxAmount, BigDecimal totalAmount, List<Line> lines) {
        public static OrderConfirmedPayload of(PurchaseOrder o) {
            return new OrderConfirmedPayload(o.getOrderId().toString(), o.getNumber(), o.getSupplierId(),
                    o.getSupplierName(), o.getShipToAddress(), o.getCurrency(), o.untaxedAmount(), o.taxAmount(),
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
