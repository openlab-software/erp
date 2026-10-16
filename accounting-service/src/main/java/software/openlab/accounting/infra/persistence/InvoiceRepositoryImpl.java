package software.openlab.accounting.infra.persistence;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import software.openlab.accounting.domain.invoice.Invoice;
import software.openlab.accounting.domain.invoice.InvoiceFilter;
import software.openlab.accounting.domain.invoice.InvoiceId;
import software.openlab.accounting.domain.invoice.InvoiceKind;
import software.openlab.accounting.domain.invoice.InvoiceLine;
import software.openlab.accounting.domain.invoice.InvoiceRepository;
import software.openlab.accounting.domain.invoice.InvoiceState;
import software.openlab.accounting.domain.invoice.Payment;
import software.openlab.accounting.domain.invoice.PaymentId;
import software.openlab.accounting.domain.invoice.PaymentMethod;
import software.openlab.accounting.domain.invoice.PaymentState;
import software.openlab.accounting.domain.shared.PageResult;

@ApplicationScoped
public class InvoiceRepositoryImpl implements InvoiceRepository {

    @Inject
    EntityManager em;

    @Override
    public Optional<Invoice> findById(InvoiceId invoiceId) {
        return findEntity(invoiceId).map(this::toDomain);
    }

    @Override
    public boolean existsByOrigin(InvoiceKind kind, String originType, String originId) {
        return InvoiceEntity.count("kind = ?1 and originType = ?2 and originId = ?3", kind.name(), originType,
                originId) > 0;
    }

    @Override
    public PageResult<Invoice> find(InvoiceFilter f, int page, int pageSize) {
        List<String> conditions = new ArrayList<>();
        Parameters params = new Parameters();
        if (f.q() != null && !f.q().isBlank()) {
            conditions.add("(lower(number) like :q or lower(reference) like :q or lower(partnerName) like :q"
                    + " or lower(originNumber) like :q)");
            params.and("q", "%" + f.q().trim().toLowerCase() + "%");
        }
        if (f.kind() != null) {
            conditions.add("kind = :kind");
            params.and("kind", f.kind().name());
        }
        if (f.state() != null) {
            conditions.add("state = :state");
            params.and("state", f.state().name());
        }
        if (f.paymentState() != null) {
            conditions.add("paymentState = :paymentState");
            params.and("paymentState", f.paymentState().name());
        }
        if (f.partnerId() != null) {
            conditions.add("partnerId = :partner");
            params.and("partner", f.partnerId());
        }
        if (f.overdueOn() != null) {
            conditions.add("state = :posted and paymentState <> :paid and dueDate < :today");
            params.and("posted", InvoiceState.POSTED.name());
            params.and("paid", PaymentState.PAID.name());
            params.and("today", f.overdueOn());
        }

        Sort sort = Sort.by("createdAt", Sort.Direction.Descending);
        var query = conditions.isEmpty()
                ? InvoiceEntity.find("", sort)
                : InvoiceEntity.find(String.join(" and ", conditions), sort, params);

        long total = query.count();
        List<Invoice> data = query.page(Page.of(page - 1, pageSize)).<InvoiceEntity>list().stream()
                .map(this::toDomain).toList();
        return new PageResult<>(data, page, pageSize, total);
    }

    @Override
    public String nextNumber(InvoiceKind kind) {
        String sequence = kind == InvoiceKind.CUSTOMER_INVOICE
                ? "accounting.customer_invoice_number_seq" : "accounting.vendor_bill_number_seq";
        Number next = (Number) em.createNativeQuery("select nextval('" + sequence + "')").getSingleResult();
        return String.format("%s/%05d", kind.numberPrefix(), next.longValue());
    }

    @Override
    public void insert(Invoice invoice) {
        InvoiceEntity entity = new InvoiceEntity();
        entity.publicId = invoice.getInvoiceId().toString();
        entity.kind = invoice.getKind().name();
        entity.createdAt = invoice.getCreatedAt();
        copyToEntity(invoice, entity);
        InvoiceEntity.persist(entity);
    }

    @Override
    public void update(Invoice invoice) {
        InvoiceEntity entity = findEntity(invoice.getInvoiceId())
                .orElseThrow(() -> new IllegalStateException("invoice not found: " + invoice.getInvoiceId()));
        copyToEntity(invoice, entity);
        entity.updatedAt = Instant.now();
    }

    @Override
    public void deleteById(InvoiceId invoiceId) {
        findEntity(invoiceId).ifPresent(InvoiceEntity::delete);
    }

    @Override
    public Position position(InvoiceKind kind, LocalDate today) {
        Object[] open = em.createQuery(
                        "select count(i), coalesce(sum(i.totalAmount - i.paidAmount), 0) from InvoiceEntity i"
                                + " where i.kind = :kind and i.state = :posted and i.paymentState <> :paid",
                        Object[].class)
                .setParameter("kind", kind.name())
                .setParameter("posted", InvoiceState.POSTED.name())
                .setParameter("paid", PaymentState.PAID.name())
                .getSingleResult();
        Object[] overdue = em.createQuery(
                        "select count(i), coalesce(sum(i.totalAmount - i.paidAmount), 0) from InvoiceEntity i"
                                + " where i.kind = :kind and i.state = :posted and i.paymentState <> :paid"
                                + " and i.dueDate < :today",
                        Object[].class)
                .setParameter("kind", kind.name())
                .setParameter("posted", InvoiceState.POSTED.name())
                .setParameter("paid", PaymentState.PAID.name())
                .setParameter("today", today)
                .getSingleResult();
        long drafts = InvoiceEntity.count("kind = ?1 and state = ?2", kind.name(), InvoiceState.DRAFT.name());
        return new Position(kind, (Long) open[0], toDecimal(open[1]), (Long) overdue[0], toDecimal(overdue[1]),
                drafts);
    }

    private Optional<InvoiceEntity> findEntity(InvoiceId invoiceId) {
        return InvoiceEntity.<InvoiceEntity>find("publicId", invoiceId.toString()).firstResultOptional();
    }

    private void copyToEntity(Invoice i, InvoiceEntity e) {
        e.number = i.getNumber();
        e.state = i.getState().name();
        e.paymentState = i.getPaymentState().name();
        e.partnerId = i.getPartnerId();
        e.partnerName = i.getPartnerName();
        e.currency = i.getCurrency();
        e.invoiceDate = i.getInvoiceDate();
        e.dueDate = i.getDueDate();
        e.reference = i.getReference();
        e.note = i.getNote();
        e.originType = i.getOriginType();
        e.originId = i.getOriginId();
        e.originNumber = i.getOriginNumber();
        e.untaxedAmount = i.untaxedAmount();
        e.taxAmount = i.taxAmount();
        e.totalAmount = i.totalAmount();
        e.paidAmount = i.paidAmount();
        e.postedAt = i.getPostedAt();

        e.lines.clear();
        int position = 0;
        for (InvoiceLine line : i.getLines()) {
            InvoiceLineEntity l = new InvoiceLineEntity();
            l.invoice = e;
            l.position = position++;
            l.productId = line.productId();
            l.description = line.description();
            l.quantity = line.quantity();
            l.unitPrice = line.unitPrice();
            l.discountPercent = line.discountPercent();
            l.taxPercent = line.taxPercent();
            e.lines.add(l);
        }

        // Payments are append-only: only the ones not stored yet are added.
        for (Payment payment : i.getPayments()) {
            boolean stored = e.payments.stream().anyMatch(p -> p.publicId.equals(payment.paymentId().toString()));
            if (!stored) {
                PaymentEntity p = new PaymentEntity();
                p.publicId = payment.paymentId().toString();
                p.invoice = e;
                p.amount = payment.amount();
                p.paidOn = payment.paidOn();
                p.method = payment.method().name();
                p.reference = payment.reference();
                p.createdAt = payment.createdAt();
                e.payments.add(p);
            }
        }
    }

    private Invoice toDomain(InvoiceEntity e) {
        List<InvoiceLine> lines = e.lines.stream()
                .map(l -> new InvoiceLine(l.productId, l.description, l.quantity, l.unitPrice, l.discountPercent,
                        l.taxPercent))
                .toList();
        List<Payment> payments = new ArrayList<>(e.payments.stream()
                .map(p -> new Payment(PaymentId.of(p.publicId), p.amount, p.paidOn, PaymentMethod.valueOf(p.method),
                        p.reference, p.createdAt))
                .toList());
        return new Invoice(InvoiceId.of(e.publicId), InvoiceKind.valueOf(e.kind), e.number,
                InvoiceState.valueOf(e.state), PaymentState.valueOf(e.paymentState), e.partnerId, e.partnerName,
                e.currency, e.invoiceDate, e.dueDate, e.reference, e.note, e.originType, e.originId,
                e.originNumber, lines, payments, e.postedAt, e.createdAt, e.updatedAt);
    }

    private static BigDecimal toDecimal(Object value) {
        return value instanceof BigDecimal d ? d : new BigDecimal(String.valueOf(value));
    }
}
