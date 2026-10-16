package software.openlab.stock.domain.picking;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.openlab.stock.domain.shared.BadRequestException;
import software.openlab.stock.domain.shared.ConflictException;
import software.openlab.stock.domain.shared.ProductId;
import software.openlab.stock.domain.stock.StockId;

/**
 * A stock transfer (Odoo's "picking"): a receipt, a delivery or an internal move. DRAFT and CONFIRMED pickings
 * can be edited; validating one moves the goods (stock movements are applied by the use case), and a partial
 * validation leaves the remainder to a backorder.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Picking {

    private PickingId pickingId;
    /** Human reference, e.g. IN/00001, OUT/00001, INT/00001. */
    private String number;
    private PickingType type;
    private PickingState state;
    private StockId sourceStockId;
    private StockId destinationStockId;
    /** Customer (deliveries) or supplier (receipts) the goods go to / come from. Informational. */
    private String partnerId;
    private String partnerName;
    /** Document that originated it (SALES_ORDER / PURCHASE_ORDER), when created by another module. */
    private String originType;
    private String originId;
    private String originNumber;
    /** The picking this one completes after a partial validation. */
    private PickingId backorderOf;
    private LocalDate scheduledDate;
    private String note;
    private List<PickingLine> lines;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant doneAt;

    public static Picking newPicking(String number, PickingType type, PickingCommand command) {
        Picking picking = new Picking();
        picking.pickingId = PickingId.generate();
        picking.number = number;
        picking.type = type;
        picking.state = PickingState.DRAFT;
        picking.createdAt = Instant.now();
        picking.apply(command);
        return picking;
    }

    public void setOrigin(String type, String id, String number) {
        this.originType = type;
        this.originId = id;
        this.originNumber = number;
    }

    public boolean isEditable() {
        return state == PickingState.DRAFT || state == PickingState.CONFIRMED;
    }

    /** Replaces every editable field and all lines (PUT semantics). */
    public void apply(PickingCommand c) {
        if (!isEditable()) {
            throw new ConflictException("error.picking.notEditable", state);
        }
        this.sourceStockId = c.sourceStockId();
        this.destinationStockId = c.destinationStockId();
        this.partnerId = c.partnerId();
        this.partnerName = c.partnerName();
        this.scheduledDate = c.scheduledDate();
        this.note = c.note();
        this.lines = new ArrayList<>(c.lines());
    }

    public void confirm() {
        if (state != PickingState.DRAFT) {
            throw new ConflictException("error.picking.notDraft", state);
        }
        if (lines.isEmpty()) {
            throw new ConflictException("error.picking.empty");
        }
        state = PickingState.CONFIRMED;
    }

    public void cancel() {
        if (state == PickingState.DONE) {
            throw new ConflictException("error.picking.alreadyDone");
        }
        if (state == PickingState.CANCELLED) {
            throw new ConflictException("error.picking.alreadyCancelled");
        }
        state = PickingState.CANCELLED;
    }

    /** The stocks the goods leave from / arrive at must be known before the picking can be validated. */
    public void requireStocks() {
        if (type != PickingType.RECEIPT && sourceStockId == null) {
            throw new ConflictException("error.picking.sourceRequired");
        }
        if (type != PickingType.DELIVERY && destinationStockId == null) {
            throw new ConflictException("error.picking.destinationRequired");
        }
    }

    /**
     * Records what was actually moved ({@code doneByProduct}; a product that is absent moves its whole demand)
     * and closes the picking. Returns the lines still pending, which the caller turns into a backorder.
     */
    public List<PickingLine> complete(Map<ProductId, Integer> doneByProduct) {
        if (!isEditable()) {
            throw new ConflictException("error.picking.notEditable", state);
        }
        if (lines.isEmpty()) {
            throw new ConflictException("error.picking.empty");
        }
        for (ProductId product : doneByProduct.keySet()) {
            if (lines.stream().noneMatch(l -> l.productId().equals(product))) {
                throw new BadRequestException("error.picking.productNotInPicking", product);
            }
        }

        List<PickingLine> completed = new ArrayList<>();
        List<PickingLine> pending = new ArrayList<>();
        int total = 0;
        for (PickingLine line : lines) {
            int done = doneByProduct.getOrDefault(line.productId(), line.demand());
            if (done < 0 || done > line.demand()) {
                throw new BadRequestException("error.picking.doneInvalid", line.productId(), line.demand());
            }
            total += done;
            PickingLine result = new PickingLine(line.productId(), line.description(), line.demand(), done);
            completed.add(result);
            if (result.remaining() > 0) {
                pending.add(new PickingLine(line.productId(), line.description(), result.remaining(), 0));
            }
        }
        if (total == 0) {
            throw new ConflictException("error.picking.nothingDone");
        }
        this.lines = completed;
        this.state = PickingState.DONE;
        this.doneAt = Instant.now();
        return pending;
    }
}
