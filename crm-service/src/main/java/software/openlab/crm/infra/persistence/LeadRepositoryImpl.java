package software.openlab.crm.infra.persistence;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import software.openlab.crm.domain.lead.Lead;
import software.openlab.crm.domain.lead.LeadFilter;
import software.openlab.crm.domain.lead.LeadId;
import software.openlab.crm.domain.lead.LeadRepository;
import software.openlab.crm.domain.lead.LeadStatus;
import software.openlab.crm.domain.lead.LeadType;
import software.openlab.crm.domain.lostreason.LostReasonId;
import software.openlab.crm.domain.shared.PageResult;
import software.openlab.crm.domain.stage.StageId;

@ApplicationScoped
public class LeadRepositoryImpl implements LeadRepository {

    @Inject
    EntityManager em;

    @Override
    public Optional<Lead> findById(LeadId leadId) {
        return findEntity(leadId).map(this::toDomain);
    }

    @Override
    public PageResult<Lead> find(LeadFilter f, int page, int pageSize) {
        List<String> conditions = new ArrayList<>();
        Parameters params = new Parameters();

        if (f.q() != null && !f.q().isBlank()) {
            conditions.add("(lower(name) like :q or lower(contactName) like :q or lower(companyName) like :q"
                    + " or lower(email) like :q)");
            params.and("q", "%" + f.q().trim().toLowerCase() + "%");
        }
        if (f.type() != null) {
            conditions.add("type = :type");
            params.and("type", f.type().name());
        }
        if (f.status() != null) {
            conditions.add("status = :status");
            params.and("status", f.status().name());
        }
        if (f.stageId() != null) {
            conditions.add("stage.publicId = :stage");
            params.and("stage", f.stageId().toString());
        }
        if (f.customerId() != null) {
            conditions.add("customerId = :customer");
            params.and("customer", f.customerId());
        }
        if (f.assignedTo() != null && !f.assignedTo().isBlank()) {
            conditions.add("assignedTo = :assignee");
            params.and("assignee", f.assignedTo());
        }
        if (f.tag() != null && !f.tag().isBlank()) {
            conditions.add(":tag member of tags");
            params.and("tag", f.tag().trim().toLowerCase());
        }

        Sort sort = Sort.by("priority", Sort.Direction.Descending).and("createdAt", Sort.Direction.Descending);
        var query = conditions.isEmpty()
                ? LeadEntity.find("", sort)
                : LeadEntity.find(String.join(" and ", conditions), sort, params);

        long total = query.count();
        List<Lead> data = query.page(Page.of(page - 1, pageSize)).<LeadEntity>list().stream()
                .map(this::toDomain).toList();
        return new PageResult<>(data, page, pageSize, total);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<StageTotals> openTotalsByStage() {
        List<Object[]> rows = em.createQuery(
                "select l.stage.publicId, count(l), coalesce(sum(l.expectedRevenue), 0),"
                        + " coalesce(sum(l.expectedRevenue * l.probability / 100), 0)"
                        + " from LeadEntity l where l.status = :open group by l.stage.publicId")
                .setParameter("open", LeadStatus.OPEN.name())
                .getResultList();
        return rows.stream()
                .map(r -> new StageTotals(StageId.of((String) r[0]), (Long) r[1], toDecimal(r[2]), toDecimal(r[3])))
                .toList();
    }

    @Override
    public void insert(Lead lead) {
        LeadEntity entity = new LeadEntity();
        entity.publicId = lead.getLeadId().toString();
        entity.createdAt = lead.getCreatedAt();
        copyToEntity(lead, entity);
        LeadEntity.persist(entity);
    }

    @Override
    public void update(Lead lead) {
        LeadEntity entity = findEntity(lead.getLeadId())
                .orElseThrow(() -> new IllegalStateException("lead not found: " + lead.getLeadId()));
        copyToEntity(lead, entity);
        entity.updatedAt = Instant.now();
    }

    @Override
    public void deleteById(LeadId leadId) {
        // Loading the entity lets Hibernate clear the tag collection; activities go via ON DELETE CASCADE.
        findEntity(leadId).ifPresent(LeadEntity::delete);
    }

    private Optional<LeadEntity> findEntity(LeadId leadId) {
        return LeadEntity.<LeadEntity>find("publicId", leadId.toString()).firstResultOptional();
    }

    private void copyToEntity(Lead l, LeadEntity e) {
        e.name = l.getName();
        e.type = l.getType().name();
        e.status = l.getStatus().name();
        e.stage = StageEntity.<StageEntity>find("publicId", l.getStageId().toString()).firstResult();
        e.customerId = l.getCustomerId();
        e.contactName = l.getContactName();
        e.companyName = l.getCompanyName();
        e.email = l.getEmail();
        e.phone = l.getPhone();
        e.expectedRevenue = l.getExpectedRevenue();
        e.probability = l.getProbability();
        e.priority = l.getPriority();
        e.expectedClosing = l.getExpectedClosing();
        e.description = l.getDescription();
        e.assignedTo = l.getAssignedTo();
        e.lostReason = l.getLostReasonId() == null ? null
                : LostReasonEntity.<LostReasonEntity>find("publicId", l.getLostReasonId().toString()).firstResult();
        e.tags.clear();
        if (l.getTags() != null) {
            e.tags.addAll(l.getTags());
        }
        e.closedAt = l.getClosedAt();
    }

    private Lead toDomain(LeadEntity e) {
        return new Lead(
                LeadId.of(e.publicId), e.name, LeadType.valueOf(e.type), LeadStatus.valueOf(e.status),
                StageId.of(e.stage.publicId), e.customerId, e.contactName, e.companyName, e.email, e.phone,
                e.expectedRevenue, e.probability, e.priority, e.expectedClosing, e.description, e.assignedTo,
                new LinkedHashSet<>(e.tags),
                e.lostReason == null ? null : LostReasonId.of(e.lostReason.publicId),
                e.createdAt, e.updatedAt, e.closedAt);
    }

    private static BigDecimal toDecimal(Object value) {
        return value instanceof BigDecimal d ? d : new BigDecimal(String.valueOf(value));
    }
}
