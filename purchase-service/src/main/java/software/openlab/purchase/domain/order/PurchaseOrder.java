package software.openlab.purchase.domain.order;

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
import software.openlab.purchase.domain.shared.ConflictException;

/**
 * A RFQ that becomes a purchase order once confirmed. While DRAFT or SENT it is freely editable; after
 * confirmation only its fulfilment/billing status moves (driven by events from other modules) until it is
 * locked (DONE) or cancelled.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrder {

    private PurchaseOrderId orderId;
    /** Human reference, e.g. S00042. */
    private String number;
    private OrderState state;
    private String supplierId;
    private String supplierName;
    /** Opportunity (crm-service) the RFQ came from; optional. */
    private String origin;
    private String currency;
    private LocalDate expectedDate;
    private String shipToAddress;
    private String buyer;
    private String note;
    private ReceiptStatus receiptStatus;
    private BillStatus billStatus;
    private List<OrderLine> lines;
    private Instant confirmedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public static PurchaseOrder newRFQ(String number, OrderCommand command) {
        PurchaseOrder order = new PurchaseOrder();
        order.orderId = PurchaseOrderId.generate();
        order.number = number;
        order.state = OrderState.DRAFT;
        order.receiptStatus = ReceiptStatus.NONE;
        order.billStatus = BillStatus.NONE;
        order.createdAt = Instant.now();
        order.apply(command);
        return order;
    }

    /** Replaces every editable field and all lines (PUT semantics). Only RFQs can be edited. */
    public void apply(OrderCommand c) {
        if (!isEditable()) {
            throw new ConflictException("error.order.notEditable", state);
        }
        this.supplierId = c.supplierId();
        this.supplierName = c.supplierName();
        this.origin = c.origin();
        this.currency = c.currency();
        this.expectedDate = c.expectedDate();
        this.shipToAddress = c.shipToAddress();
        this.buyer = c.buyer();
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

    /** Turns the RFQ into a purchase order. */
    public void confirm() {
        if (!isEditable()) {
            throw new ConflictException("error.order.notEditable", state);
        }
        if (lines.isEmpty()) {
            throw new ConflictException("error.order.empty");
        }
        state = OrderState.CONFIRMED;
        confirmedAt = Instant.now();
        // An order of services only has nothing to deliver.
        receiptStatus = lines.stream().anyMatch(OrderLine::isGoods) ? ReceiptStatus.PENDING : ReceiptStatus.NONE;
        billStatus = BillStatus.TO_BILL;
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
        if (receiptStatus == ReceiptStatus.PARTIAL || receiptStatus == ReceiptStatus.DONE
                || billStatus == BillStatus.BILLED) {
            throw new ConflictException("error.order.hasFulfilment");
        }
        state = OrderState.CANCELLED;
        receiptStatus = ReceiptStatus.NONE;
        billStatus = BillStatus.NONE;
    }

    /** CANCELLED back to a draft RFQ. */
    public void resetToDraft() {
        if (state != OrderState.CANCELLED) {
            throw new ConflictException("error.order.notCancelled", state);
        }
        state = OrderState.DRAFT;
        confirmedAt = null;
    }

    /** Adds received quantities per product (from a validated stock transfer) and recomputes the status. */
    public void applyReceipt(Map<String, BigDecimal> receivedByProduct) {
        if (state != OrderState.CONFIRMED && state != OrderState.DONE) {
            return;
        }
        Map<String, BigDecimal> pending = new java.util.HashMap<>(receivedByProduct);
        List<OrderLine> updated = new ArrayList<>();
        for (OrderLine line : lines) {
            BigDecimal incoming = pending.getOrDefault(line.productId(), BigDecimal.ZERO);
            if (incoming.signum() > 0) {
                // A product repeated on several lines is received line by line, up to each line's quantity.
                BigDecimal room = line.quantity().subtract(line.receivedQuantity()).max(BigDecimal.ZERO);
                BigDecimal applied = incoming.min(room);
                pending.put(line.productId(), incoming.subtract(applied));
                line = line.withReceived(line.receivedQuantity().add(applied));
            }
            updated.add(line);
        }
        this.lines = updated;
        List<OrderLine> goods = lines.stream().filter(OrderLine::isGoods).toList();
        if (goods.isEmpty()) {
            return;
        }
        boolean any = goods.stream().anyMatch(l -> l.receivedQuantity().signum() > 0);
        boolean all = goods.stream().allMatch(OrderLine::isFullyReceived);
        receiptStatus = all ? ReceiptStatus.DONE : any ? ReceiptStatus.PARTIAL : ReceiptStatus.PENDING;
    }

    public void markBilled() {
        if (state == OrderState.CONFIRMED || state == OrderState.DONE) {
            billStatus = BillStatus.BILLED;
        }
    }
}
