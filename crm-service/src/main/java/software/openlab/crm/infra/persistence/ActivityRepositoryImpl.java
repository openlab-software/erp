package software.openlab.crm.infra.persistence;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import software.openlab.crm.domain.activity.Activity;
import software.openlab.crm.domain.activity.ActivityId;
import software.openlab.crm.domain.activity.ActivityRepository;
import software.openlab.crm.domain.activity.ActivityType;
import software.openlab.crm.domain.lead.LeadId;
import software.openlab.crm.domain.shared.PageResult;

@ApplicationScoped
public class ActivityRepositoryImpl implements ActivityRepository {

    @Override
    public Optional<Activity> findById(LeadId leadId, ActivityId activityId) {
        return findEntity(leadId, activityId).map(this::toDomain);
    }

    @Override
    public List<Activity> findByLead(LeadId leadId) {
        return ActivityEntity.<ActivityEntity>list("lead.publicId = ?1 order by done, dueDate, id", leadId.toString())
                .stream().map(this::toDomain).toList();
    }

    @Override
    public PageResult<Activity> find(Boolean done, LocalDate dueBefore, String assignedTo, int page, int pageSize) {
        List<String> conditions = new ArrayList<>();
        Parameters params = new Parameters();
        if (done != null) {
            conditions.add("done = :done");
            params.and("done", done);
        }
        if (dueBefore != null) {
            conditions.add("dueDate <= :due");
            params.and("due", dueBefore);
        }
        if (assignedTo != null && !assignedTo.isBlank()) {
            conditions.add("assignedTo = :assignee");
            params.and("assignee", assignedTo);
        }
        Sort sort = Sort.by("dueDate").and("id");
        var query = conditions.isEmpty()
                ? ActivityEntity.find("", sort)
                : ActivityEntity.find(String.join(" and ", conditions), sort, params);
        long total = query.count();
        List<Activity> data = query.page(Page.of(page - 1, pageSize)).<ActivityEntity>list().stream()
                .map(this::toDomain).toList();
        return new PageResult<>(data, page, pageSize, total);
    }

    @Override
    public void insert(Activity activity) {
        ActivityEntity entity = new ActivityEntity();
        entity.publicId = activity.getActivityId().toString();
        entity.lead = LeadEntity.<LeadEntity>find("publicId", activity.getLeadId().toString()).firstResultOptional()
                .orElseThrow(() -> new IllegalStateException("lead not found: " + activity.getLeadId()));
        entity.createdAt = activity.getCreatedAt();
        copyToEntity(activity, entity);
        ActivityEntity.persist(entity);
    }

    @Override
    public void update(Activity activity) {
        ActivityEntity entity = findEntity(activity.getLeadId(), activity.getActivityId())
                .orElseThrow(() -> new IllegalStateException("activity not found: " + activity.getActivityId()));
        copyToEntity(activity, entity);
    }

    @Override
    public void deleteById(LeadId leadId, ActivityId activityId) {
        findEntity(leadId, activityId).ifPresent(ActivityEntity::delete);
    }

    private Optional<ActivityEntity> findEntity(LeadId leadId, ActivityId activityId) {
        return ActivityEntity.<ActivityEntity>find("publicId = ?1 and lead.publicId = ?2", activityId.toString(),
                leadId.toString()).firstResultOptional();
    }

    private void copyToEntity(Activity a, ActivityEntity e) {
        e.type = a.getType().name();
        e.summary = a.getSummary();
        e.note = a.getNote();
        e.dueDate = a.getDueDate();
        e.assignedTo = a.getAssignedTo();
        e.done = a.isDone();
        e.doneAt = a.getDoneAt();
    }

    private Activity toDomain(ActivityEntity e) {
        return new Activity(ActivityId.of(e.publicId), LeadId.of(e.lead.publicId), ActivityType.valueOf(e.type),
                e.summary, e.note, e.dueDate, e.assignedTo, e.done, e.doneAt, e.createdAt);
    }
}
