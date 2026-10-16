package software.openlab.stock.infra.persistence;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import software.openlab.stock.domain.picking.Picking;
import software.openlab.stock.domain.picking.PickingFilter;
import software.openlab.stock.domain.picking.PickingId;
import software.openlab.stock.domain.picking.PickingLine;
import software.openlab.stock.domain.picking.PickingRepository;
import software.openlab.stock.domain.picking.PickingState;
import software.openlab.stock.domain.picking.PickingType;
import software.openlab.stock.domain.shared.NotFoundException;
import software.openlab.stock.domain.shared.PageResult;
import software.openlab.stock.domain.shared.ProductId;
import software.openlab.stock.domain.stock.StockId;

@ApplicationScoped
public class PanachePickingRepository implements PickingRepository {

    @Inject
    EntityManager em;

    @Override
    public Optional<Picking> findById(PickingId pickingId) {
        return findEntity(pickingId).map(this::toDomain);
    }

    @Override
    public PageResult<Picking> find(PickingFilter f, int page, int pageSize) {
        List<String> conditions = new ArrayList<>();
        Parameters params = new Parameters();
        if (f.q() != null && !f.q().isBlank()) {
            conditions.add("(lower(number) like :q or lower(partnerName) like :q or lower(originNumber) like :q)");
            params.and("q", "%" + f.q().trim().toLowerCase() + "%");
        }
        if (f.type() != null) {
            conditions.add("type = :type");
            params.and("type", f.type().name());
        }
        if (f.state() != null) {
            conditions.add("state = :state");
            params.and("state", f.state().name());
        }
        if (f.originType() != null && !f.originType().isBlank()) {
            conditions.add("originType = :originType");
            params.and("originType", f.originType());
        }
        if (f.originId() != null && !f.originId().isBlank()) {
            conditions.add("originId = :originId");
            params.and("originId", f.originId());
        }
        if (f.stockId() != null) {
            conditions.add("(sourceStock.publicId = :stock or destinationStock.publicId = :stock)");
            params.and("stock", f.stockId().toString());
        }

        Sort sort = Sort.by("createdAt", Sort.Direction.Descending);
        var query = conditions.isEmpty()
                ? PickingEntity.find("", sort)
                : PickingEntity.find(String.join(" and ", conditions), sort, params);

        long total = query.count();
        List<Picking> data = query.page(Page.of(page - 1, pageSize)).<PickingEntity>list().stream()
                .map(this::toDomain).toList();
        return new PageResult<>(data, page, pageSize, total);
    }

    @Override
    public boolean existsByOrigin(PickingType type, String originType, String originId) {
        return PickingEntity.count("type = ?1 and originType = ?2 and originId = ?3", type.name(), originType,
                originId) > 0;
    }

    @Override
    public List<Picking> findOpenByOrigin(String originType, String originId) {
        return PickingEntity.<PickingEntity>list("originType = ?1 and originId = ?2 and state in (?3, ?4)",
                        originType, originId, PickingState.DRAFT.name(), PickingState.CONFIRMED.name())
                .stream().map(this::toDomain).toList();
    }

    @Override
    public String nextNumber(PickingType type) {
        String sequence = switch (type) {
            case RECEIPT -> "stock.picking_receipt_seq";
            case DELIVERY -> "stock.picking_delivery_seq";
            case INTERNAL -> "stock.picking_internal_seq";
        };
        Number next = (Number) em.createNativeQuery("select nextval('" + sequence + "')").getSingleResult();
        return String.format("%s/%05d", type.numberPrefix(), next.longValue());
    }

    @Override
    public void insert(Picking picking) {
        PickingEntity entity = new PickingEntity();
        entity.publicId = picking.getPickingId().toString();
        entity.number = picking.getNumber();
        entity.type = picking.getType().name();
        entity.createdAt = picking.getCreatedAt();
        copyToEntity(picking, entity);
        PickingEntity.persist(entity);
    }

    @Override
    public void update(Picking picking) {
        PickingEntity entity = findEntity(picking.getPickingId())
                .orElseThrow(() -> new IllegalStateException("picking not found: " + picking.getPickingId()));
        copyToEntity(picking, entity);
        entity.updatedAt = Instant.now();
    }

    @Override
    public void deleteById(PickingId pickingId) {
        findEntity(pickingId).ifPresent(PickingEntity::delete);
    }

    private Optional<PickingEntity> findEntity(PickingId pickingId) {
        return PickingEntity.<PickingEntity>find("publicId", pickingId.toString()).firstResultOptional();
    }

    private StockEntity stockEntity(StockId stockId) {
        if (stockId == null) {
            return null;
        }
        return StockEntity.<StockEntity>find("publicId", stockId.toString()).firstResultOptional()
                .orElseThrow(() -> new NotFoundException("error.stock.notFound"));
    }

    private void copyToEntity(Picking p, PickingEntity e) {
        e.state = p.getState().name();
        e.sourceStock = stockEntity(p.getSourceStockId());
        e.destinationStock = stockEntity(p.getDestinationStockId());
        e.partnerId = p.getPartnerId();
        e.partnerName = p.getPartnerName();
        e.originType = p.getOriginType();
        e.originId = p.getOriginId();
        e.originNumber = p.getOriginNumber();
        e.backorderOf = p.getBackorderOf() == null ? null : p.getBackorderOf().toString();
        e.scheduledDate = p.getScheduledDate();
        e.note = p.getNote();
        e.doneAt = p.getDoneAt();

        e.lines.clear();
        int position = 0;
        for (PickingLine line : p.getLines()) {
            PickingLineEntity l = new PickingLineEntity();
            l.picking = e;
            l.position = position++;
            l.catalogProductPublicId = line.productId().toString();
            l.description = line.description();
            l.demand = line.demand();
            l.done = line.done();
            e.lines.add(l);
        }
    }

    private Picking toDomain(PickingEntity e) {
        List<PickingLine> lines = e.lines.stream()
                .map(l -> new PickingLine(ProductId.of(l.catalogProductPublicId), l.description, l.demand, l.done))
                .toList();
        return new Picking(PickingId.of(e.publicId), e.number, PickingType.valueOf(e.type),
                PickingState.valueOf(e.state),
                e.sourceStock == null ? null : StockId.of(e.sourceStock.publicId),
                e.destinationStock == null ? null : StockId.of(e.destinationStock.publicId),
                e.partnerId, e.partnerName, e.originType, e.originId, e.originNumber,
                e.backorderOf == null ? null : PickingId.of(e.backorderOf), e.scheduledDate, e.note, lines,
                e.createdAt, e.updatedAt, e.doneAt);
    }
}
