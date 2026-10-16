package software.openlab.crm.domain.activity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import software.openlab.crm.domain.lead.LeadId;
import software.openlab.crm.domain.shared.PageResult;

public interface ActivityRepository {

    Optional<Activity> findById(LeadId leadId, ActivityId activityId);

    /** Activities of one lead: pending first (by due date), then done (most recent first). */
    List<Activity> findByLead(LeadId leadId);

    /** Cross-lead agenda. done, dueBefore (inclusive) and assignedTo may be null; ordered by due date. */
    PageResult<Activity> find(Boolean done, LocalDate dueBefore, String assignedTo, int page, int pageSize);

    void insert(Activity activity);

    void update(Activity activity);

    void deleteById(LeadId leadId, ActivityId activityId);
}
