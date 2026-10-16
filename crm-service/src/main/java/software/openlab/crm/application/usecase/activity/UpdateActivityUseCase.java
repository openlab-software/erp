package software.openlab.crm.application.usecase.activity;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import software.openlab.crm.domain.activity.Activity;
import software.openlab.crm.domain.activity.ActivityId;
import software.openlab.crm.domain.activity.ActivityRepository;
import software.openlab.crm.domain.activity.ActivityType;
import software.openlab.crm.domain.lead.LeadId;

@ApplicationScoped
public class UpdateActivityUseCase {

    @Inject
    ActivityRepository repository;

    @Inject
    GetActivityUseCase getActivity;

    @Transactional
    public Activity execute(LeadId leadId, ActivityId activityId, String type, String summary, String note,
                            LocalDate dueDate, String assignedTo) {
        Activity activity = getActivity.execute(leadId, activityId);
        activity.apply(type == null || type.isBlank() ? activity.getType() : ActivityType.parse(type), summary, note,
                dueDate, assignedTo);
        repository.update(activity);
        return getActivity.execute(leadId, activityId);
    }
}
