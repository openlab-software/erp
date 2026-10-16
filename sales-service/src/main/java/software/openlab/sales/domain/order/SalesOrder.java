package software.openlab.sales.domain.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.openlab.sales.domain.shared.ConflictException;

/**
 * A quotation that becomes a sales order once confirmed. While DRAFT or SENT it is freely editable; after
 * confirmation only its fulfilment/billing status moves (driven by events from other modules) until it is
 * locked (DONE) or cancelled.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrder {

    private SalesOrderId orderId;
    /** Human reference, e.g. S00042. */
    private String number;
    private OrderState state;
    private String customerId;
    private String customerName;
    /** Opportunity (crm-service) the quotation came from; optional. */
    private String leadId;
    private String currency;
    private LocalDate validUntil;
    private String deliveryAddress;
    private String salesperson;
    private String note;
    private DeliveryStatus deliveryStatus;
    private InvoiceStatus invoiceStatus;
    private List<OrderLine> lines;
    private Instant confirmedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public static SalesOrder newQuotation(String number, OrderCommand command) {
        SalesOrder order = new SalesOrder();
        order.orderId = SalesOrderId.generate();
        order.number = number;
        order.state = OrderState.DRAFT;
        order.deliveryStatus = DeliveryStatus.NONE;
        order.invoiceStatus = InvoiceStatus.NONE;
        order.createdAt = Instant.now();
        order.apply(command);
        return order;
    }

    /** Replaces every editable field and all lines (PUT semantics). Only quotations can be edited. */
    public void apply(OrderCommand c) {
        if (!isEditable()) {
            throw new ConflictException("error.order.notEditable", state);
        }
        this.customerId = c.customerId();
        this.customerName = c.customerName();
        this.leadId = c.leadId();
        this.currency = c.currency();
        this.validUntil = c.validUntil();
        this.deliveryAddress = c.deliveryAddress();
        this.salesperson = c.salesperson();
        this.note = c.note();
        this.lines = new ArrayList<>(c.lines());
    }

    public boolean isEditable() {
        return state == OrderState.DRAFT || state == OrderState.SENT;
    }

    public BigDecimal untaxedAmount() {
        return lines.stream().map(OrderLine::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal taxAmount() {
        return lines.stream().map(OrderLine::taxAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal totalAmount() {
        return untaxedAmount().add(taxAmount());
    }

    public void markSent() {
        if (state != OrderState.DRAFT) {
            throw new ConflictException("error.order.notDraft", state);
        }
        state = OrderState.SENT;
    }

    /** Turns the quotation into a sales order. */
    public void confirm() {
        if (!isEditable()) {
            throw new ConflictException("error.order.notEditable", state);
        }
        if (lines.isEmpty()) {
            throw new ConflictException("error.order.empty");
        }
        if (validUntil != null && validUntil.isBefore(LocalDate.now())) {
            throw new ConflictException("error.order.expired", validUntil);
        }
        state = OrderState.CONFIRMED;
        confirmedAt = Instant.now();
        // An order of services only has nothing to deliver.
        deliveryStatus = lines.stream().anyMatch(OrderLine::isGoods) ? DeliveryStatus.PENDING : DeliveryStatus.NONE;
        invoiceStatus = InvoiceStatus.TO_INVOICE;
    }

    /** A confirmed order stops accepting changes (Odoo's "Lock"). */
    public void lock() {
        if (state != OrderState.CONFIRMED) {
            throw new ConflictException("error.order.notConfirmed", state);
        }
        state = OrderState.DONE;
    }

    public void cancel() {
        if (state == OrderState.CANCELLED) {
            throw new ConflictException("error.order.alreadyCancelled");
        }
        if (state == OrderState.DONE) {
            throw new ConflictException("error.order.locked");
        }
        if (deliveryStatus == DeliveryStatus.PARTIAL || deliveryStatus == DeliveryStatus.DONE
                || invoiceStatus == InvoiceStatus.INVOICED) {
            throw new ConflictException("error.order.hasFulfilment");
        }
        state = OrderState.CANCELLED;
        deliveryStatus = DeliveryStatus.NONE;
        invoiceStatus = InvoiceStatus.NONE;
    }

    /** CANCELLED back to a draft quotation. */
    public void resetToDraft() {
        if (state != OrderState.CANCELLED) {
            throw new ConflictException("error.order.notCancelled", state);
        }
        state = OrderState.DRAFT;
        confirmedAt = null;
    }

    /** Adds delivered quantities per product (from a validated stock transfer) and recomputes the status. */
    public void applyDelivery(Map<String, BigDecimal> deliveredByProduct) {
        if (state != OrderState.CONFIRMED && state != OrderState.DONE) {
            return;
        }
        Map<String, BigDecimal> pending = new java.util.HashMap<>(deliveredByProduct);
        List<OrderLine> updated = new ArrayList<>();
        for (OrderLine line : lines) {
            BigDecimal incoming = pending.getOrDefault(line.productId(), BigDecimal.ZERO);
            if (incoming.signum() > 0) {
                // A product repeated on several lines is delivered line by line, up to each line's quantity.
                BigDecimal room = line.quantity().subtract(line.deliveredQuantity()).max(BigDecimal.ZERO);
                BigDecimal applied = incoming.min(room);
                pending.put(line.productId(), incoming.subtract(applied));
                line = line.withDelivered(line.deliveredQuantity().add(applied));
            }
            updated.add(line);
        }
        this.lines = updated;
        List<OrderLine> goods = lines.stream().filter(OrderLine::isGoods).toList();
        if (goods.isEmpty()) {
            return;
        }
        boolean any = goods.stream().anyMatch(l -> l.deliveredQuantity().signum() > 0);
        boolean all = goods.stream().allMatch(OrderLine::isFullyDelivered);
        deliveryStatus = all ? DeliveryStatus.DONE : any ? DeliveryStatus.PARTIAL : DeliveryStatus.PENDING;
    }

    public void markInvoiced() {
        if (state == OrderState.CONFIRMED || state == OrderState.DONE) {
            invoiceStatus = InvoiceStatus.INVOICED;
        }
    }
}
