package software.openlab.crm.application.usecase.activity;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import software.openlab.crm.application.usecase.lead.GetLeadByIdUseCase;
import software.openlab.crm.domain.activity.Activity;
import software.openlab.crm.domain.activity.ActivityEvents;
import software.openlab.crm.domain.activity.ActivityRepository;
import software.openlab.crm.domain.activity.ActivityType;
import software.openlab.crm.domain.lead.LeadId;
import software.openlab.crm.domain.shared.DomainEvent;

@ApplicationScoped
public class AddActivityUseCase {

    @Inject
    ActivityRepository repository;

    @Inject
    GetLeadByIdUseCase getLeadById;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public Activity execute(LeadId leadId, String type, String summary, String note, LocalDate dueDate,
                            String assignedTo) {
        getLeadById.execute(leadId); // 404 when the lead does not exist
        Activity activity = Activity.newActivity(leadId, type == null || type.isBlank() ? ActivityType.TODO
                : ActivityType.parse(type), summary, note, dueDate, assignedTo);
        repository.insert(activity);
        events.fire(new DomainEvent(ActivityEvents.CREATED, ActivityEvents.ActivityPayload.of(activity)));
        return activity;
    }
}
