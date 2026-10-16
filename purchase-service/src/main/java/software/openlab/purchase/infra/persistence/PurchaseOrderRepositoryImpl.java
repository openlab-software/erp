package software.openlab.purchase.infra.persistence;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import software.openlab.purchase.domain.order.ReceiptStatus;
import software.openlab.purchase.domain.order.BillStatus;
import software.openlab.purchase.domain.order.OrderFilter;
import software.openlab.purchase.domain.order.OrderLine;
import software.openlab.purchase.domain.order.OrderState;
import software.openlab.purchase.domain.order.PurchaseOrder;
import software.openlab.purchase.domain.order.PurchaseOrderId;
import software.openlab.purchase.domain.order.PurchaseOrderRepository;
import software.openlab.purchase.domain.shared.PageResult;

@ApplicationScoped
public class PurchaseOrderRepositoryImpl implements PurchaseOrderRepository {

    @Inject
    EntityManager em;

    @Override
    public Optional<PurchaseOrder> findById(PurchaseOrderId orderId) {
        return findEntity(orderId).map(this::toDomain);
    }

    @Override
    public Optional<PurchaseOrder> findByNumber(String number) {
        return PurchaseOrderEntity.<PurchaseOrderEntity>find("number", number).firstResultOptional().map(this::toDomain);
    }

    @Override
    public PageResult<PurchaseOrder> find(OrderFilter f, int page, int pageSize) {
        List<String> conditions = new ArrayList<>();
        Parameters params = new Parameters();
        if (f.q() != null && !f.q().isBlank()) {
            conditions.add("(lower(number) like :q or lower(supplierName) like :q)");
            params.and("q", "%" + f.q().trim().toLowerCase() + "%");
        }
        if (f.state() != null) {
            conditions.add("state = :state");
            params.and("state", f.state().name());
        }
        if (f.supplierId() != null) {
            conditions.add("supplierId = :supplier");
            params.and("supplier", f.supplierId());
        }
        if (f.receiptStatus() != null) {
            conditions.add("receiptStatus = :receipt");
            params.and("receipt", f.receiptStatus().name());
        }
        if (f.billStatus() != null) {
            conditions.add("billStatus = :invoice");
            params.and("invoice", f.billStatus().name());
        }

        Sort sort = Sort.by("createdAt", Sort.Direction.Descending);
        var query = conditions.isEmpty()
                ? PurchaseOrderEntity.find("", sort)
                : PurchaseOrderEntity.find(String.join(" and ", conditions), sort, params);

        long total = query.count();
        List<PurchaseOrder> data = query.page(Page.of(page - 1, pageSize)).<PurchaseOrderEntity>list().stream()
                .map(this::toDomain).toList();
        return new PageResult<>(data, page, pageSize, total);
    }

    @Override
    public String nextNumber() {
        Number next = (Number) em.createNativeQuery("select nextval('purchase.purchase_order_number_seq')")
                .getSingleResult();
        return String.format("P%05d", next.longValue());
    }

    @Override
    public void insert(PurchaseOrder order) {
        PurchaseOrderEntity entity = new PurchaseOrderEntity();
        entity.publicId = order.getOrderId().toString();
        entity.number = order.getNumber();
        entity.createdAt = order.getCreatedAt();
        copyToEntity(order, entity);
        PurchaseOrderEntity.persist(entity);
    }

    @Override
    public void update(PurchaseOrder order) {
        PurchaseOrderEntity entity = findEntity(order.getOrderId())
                .orElseThrow(() -> new IllegalStateException("order not found: " + order.getOrderId()));
        copyToEntity(order, entity);
        entity.updatedAt = Instant.now();
    }

    @Override
    public void deleteById(PurchaseOrderId orderId) {
        findEntity(orderId).ifPresent(PurchaseOrderEntity::delete);
    }

    @Override
    public List<StateTotals> totalsByState() {
        // Same arithmetic as OrderLine (the order has no stored total): per line, the subtotal is rounded to the
        // cent after the discount and the tax is rounded on top of it.
        String subtotal = "round(l.quantity * l.unit_price - round(l.quantity * l.unit_price * l.discount_percent / 100, 2), 2)";
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(
                "select o.state, count(distinct o.purchase_order_id),"
                        + " coalesce(sum(" + subtotal + " + round(" + subtotal + " * l.tax_percent / 100, 2)), 0)"
                        + " from purchase.purchase_orders o left join purchase.purchase_order_lines l on l.purchase_order_id = o.purchase_order_id"
                        + " group by o.state")
                .getResultList();
        return rows.stream()
                .map(r -> new StateTotals(OrderState.valueOf((String) r[0]), ((Number) r[1]).longValue(),
                        new BigDecimal(String.valueOf(r[2])).setScale(2, java.math.RoundingMode.HALF_UP)))
                .toList();
    }

    private Optional<PurchaseOrderEntity> findEntity(PurchaseOrderId orderId) {
        return PurchaseOrderEntity.<PurchaseOrderEntity>find("publicId", orderId.toString()).firstResultOptional();
    }

    private void copyToEntity(PurchaseOrder o, PurchaseOrderEntity e) {
        e.state = o.getState().name();
        e.supplierId = o.getSupplierId();
        e.supplierName = o.getSupplierName();
        e.origin = o.getOrigin();
        e.currency = o.getCurrency();
        e.expectedDate = o.getExpectedDate();
        e.shipToAddress = o.getShipToAddress();
        e.buyer = o.getBuyer();
        e.note = o.getNote();
        e.receiptStatus = o.getReceiptStatus().name();
        e.billStatus = o.getBillStatus().name();
        e.confirmedAt = o.getConfirmedAt();

        e.lines.clear();
        int position = 0;
        for (OrderLine line : o.getLines()) {
            PurchaseOrderLineEntity l = new PurchaseOrderLineEntity();
            l.order = e;
            l.position = position++;
            l.productId = line.productId();
            l.sku = line.sku();
            l.description = line.description();
            l.quantity = line.quantity();
            l.unitPrice = line.unitPrice();
            l.discountPercent = line.discountPercent();
            l.taxPercent = line.taxPercent();
            l.receivedQuantity = line.receivedQuantity();
            l.productType = line.productType();
            e.lines.add(l);
        }
    }

    private PurchaseOrder toDomain(PurchaseOrderEntity e) {
        List<OrderLine> lines = e.lines.stream()
                .map(l -> new OrderLine(l.productId, l.sku, l.description, l.quantity, l.unitPrice,
                        l.discountPercent, l.taxPercent, l.receivedQuantity, l.productType))
                .toList();
        return new PurchaseOrder(PurchaseOrderId.of(e.publicId), e.number, OrderState.valueOf(e.state), e.supplierId,
                e.supplierName, e.origin, e.currency, e.expectedDate, e.shipToAddress, e.buyer, e.note,
                ReceiptStatus.valueOf(e.receiptStatus), BillStatus.valueOf(e.billStatus), lines,
                e.confirmedAt, e.createdAt, e.updatedAt);
    }
}
