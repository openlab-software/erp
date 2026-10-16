package software.openlab.sales.infra.persistence;

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
import software.openlab.sales.domain.order.DeliveryStatus;
import software.openlab.sales.domain.order.InvoiceStatus;
import software.openlab.sales.domain.order.OrderFilter;
import software.openlab.sales.domain.order.OrderLine;
import software.openlab.sales.domain.order.OrderState;
import software.openlab.sales.domain.order.SalesOrder;
import software.openlab.sales.domain.order.SalesOrderId;
import software.openlab.sales.domain.order.SalesOrderRepository;
import software.openlab.sales.domain.shared.PageResult;

@ApplicationScoped
public class SalesOrderRepositoryImpl implements SalesOrderRepository {

    @Inject
    EntityManager em;

    @Override
    public Optional<SalesOrder> findById(SalesOrderId orderId) {
        return findEntity(orderId).map(this::toDomain);
    }

    @Override
    public Optional<SalesOrder> findByNumber(String number) {
        return SalesOrderEntity.<SalesOrderEntity>find("number", number).firstResultOptional().map(this::toDomain);
    }

    @Override
    public PageResult<SalesOrder> find(OrderFilter f, int page, int pageSize) {
        List<String> conditions = new ArrayList<>();
        Parameters params = new Parameters();
        if (f.q() != null && !f.q().isBlank()) {
            conditions.add("(lower(number) like :q or lower(customerName) like :q)");
            params.and("q", "%" + f.q().trim().toLowerCase() + "%");
        }
        if (f.state() != null) {
            conditions.add("state = :state");
            params.and("state", f.state().name());
        }
        if (f.customerId() != null) {
            conditions.add("customerId = :customer");
            params.and("customer", f.customerId());
        }
        if (f.deliveryStatus() != null) {
            conditions.add("deliveryStatus = :delivery");
            params.and("delivery", f.deliveryStatus().name());
        }
        if (f.invoiceStatus() != null) {
            conditions.add("invoiceStatus = :invoice");
            params.and("invoice", f.invoiceStatus().name());
        }

        Sort sort = Sort.by("createdAt", Sort.Direction.Descending);
        var query = conditions.isEmpty()
                ? SalesOrderEntity.find("", sort)
                : SalesOrderEntity.find(String.join(" and ", conditions), sort, params);

        long total = query.count();
        List<SalesOrder> data = query.page(Page.of(page - 1, pageSize)).<SalesOrderEntity>list().stream()
                .map(this::toDomain).toList();
        return new PageResult<>(data, page, pageSize, total);
    }

    @Override
    public String nextNumber() {
        Number next = (Number) em.createNativeQuery("select nextval('sales.sales_order_number_seq')")
                .getSingleResult();
        return String.format("S%05d", next.longValue());
    }

    @Override
    public void insert(SalesOrder order) {
        SalesOrderEntity entity = new SalesOrderEntity();
        entity.publicId = order.getOrderId().toString();
        entity.number = order.getNumber();
        entity.createdAt = order.getCreatedAt();
        copyToEntity(order, entity);
        SalesOrderEntity.persist(entity);
    }

    @Override
    public void update(SalesOrder order) {
        SalesOrderEntity entity = findEntity(order.getOrderId())
                .orElseThrow(() -> new IllegalStateException("order not found: " + order.getOrderId()));
        copyToEntity(order, entity);
        entity.updatedAt = Instant.now();
    }

    @Override
    public void deleteById(SalesOrderId orderId) {
        findEntity(orderId).ifPresent(SalesOrderEntity::delete);
    }

    @Override
    public List<StateTotals> totalsByState() {
        // Same arithmetic as OrderLine (the order has no stored total): per line, the subtotal is rounded to the
        // cent after the discount and the tax is rounded on top of it.
        String subtotal = "round(l.quantity * l.unit_price - round(l.quantity * l.unit_price * l.discount_percent / 100, 2), 2)";
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(
                "select o.state, count(distinct o.sales_order_id),"
                        + " coalesce(sum(" + subtotal + " + round(" + subtotal + " * l.tax_percent / 100, 2)), 0)"
                        + " from sales.sales_orders o left join sales.sales_order_lines l on l.sales_order_id = o.sales_order_id"
                        + " group by o.state")
                .getResultList();
        return rows.stream()
                .map(r -> new StateTotals(OrderState.valueOf((String) r[0]), ((Number) r[1]).longValue(),
                        new BigDecimal(String.valueOf(r[2])).setScale(2, java.math.RoundingMode.HALF_UP)))
                .toList();
    }

    private Optional<SalesOrderEntity> findEntity(SalesOrderId orderId) {
        return SalesOrderEntity.<SalesOrderEntity>find("publicId", orderId.toString()).firstResultOptional();
    }

    private void copyToEntity(SalesOrder o, SalesOrderEntity e) {
        e.state = o.getState().name();
        e.customerId = o.getCustomerId();
        e.customerName = o.getCustomerName();
        e.leadId = o.getLeadId();
        e.currency = o.getCurrency();
        e.validUntil = o.getValidUntil();
        e.deliveryAddress = o.getDeliveryAddress();
        e.salesperson = o.getSalesperson();
        e.note = o.getNote();
        e.deliveryStatus = o.getDeliveryStatus().name();
        e.invoiceStatus = o.getInvoiceStatus().name();
        e.confirmedAt = o.getConfirmedAt();

        e.lines.clear();
        int position = 0;
        for (OrderLine line : o.getLines()) {
            SalesOrderLineEntity l = new SalesOrderLineEntity();
            l.order = e;
            l.position = position++;
            l.productId = line.productId();
            l.sku = line.sku();
            l.description = line.description();
            l.quantity = line.quantity();
            l.unitPrice = line.unitPrice();
            l.discountPercent = line.discountPercent();
            l.taxPercent = line.taxPercent();
            l.deliveredQuantity = line.deliveredQuantity();
            l.productType = line.productType();
            e.lines.add(l);
        }
    }

    private SalesOrder toDomain(SalesOrderEntity e) {
        List<OrderLine> lines = e.lines.stream()
                .map(l -> new OrderLine(l.productId, l.sku, l.description, l.quantity, l.unitPrice,
                        l.discountPercent, l.taxPercent, l.deliveredQuantity, l.productType))
                .toList();
        return new SalesOrder(SalesOrderId.of(e.publicId), e.number, OrderState.valueOf(e.state), e.customerId,
                e.customerName, e.leadId, e.currency, e.validUntil, e.deliveryAddress, e.salesperson, e.note,
                DeliveryStatus.valueOf(e.deliveryStatus), InvoiceStatus.valueOf(e.invoiceStatus), lines,
                e.confirmedAt, e.createdAt, e.updatedAt);
    }
}
