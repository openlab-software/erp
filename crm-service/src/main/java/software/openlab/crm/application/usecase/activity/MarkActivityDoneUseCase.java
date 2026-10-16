package software.openlab.crm.application.usecase.activity;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.crm.domain.activity.Activity;
import software.openlab.crm.domain.activity.ActivityEvents;
import software.openlab.crm.domain.activity.ActivityId;
import software.openlab.crm.domain.activity.ActivityRepository;
import software.openlab.crm.domain.lead.LeadId;
import software.openlab.crm.domain.shared.DomainEvent;

@ApplicationScoped
public class MarkActivityDoneUseCase {

    @Inject
    ActivityRepository repository;

    @Inject
    GetActivityUseCase getActivity;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public Activity execute(LeadId leadId, ActivityId activityId) {
        Activity activity = getActivity.execute(leadId, activityId);
        activity.markDone();
        repository.update(activity);
        Activity updated = getActivity.execute(leadId, activityId);
        events.fire(new DomainEvent(ActivityEvents.DONE, ActivityEvents.ActivityPayload.of(updated)));
        return updated;
    }
}
