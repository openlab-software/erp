package software.openlab.crm.application.usecase.activity;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.openlab.crm.application.usecase.lead.GetLeadByIdUseCase;
import software.openlab.crm.domain.activity.Activity;
import software.openlab.crm.domain.activity.ActivityId;
import software.openlab.crm.domain.activity.ActivityRepository;
import software.openlab.crm.domain.lead.LeadId;
import software.openlab.crm.domain.shared.NotFoundException;

@ApplicationScoped
public class GetActivityUseCase {

    @Inject
    ActivityRepository repository;

    @Inject
    GetLeadByIdUseCase getLeadById;

    public Activity execute(LeadId leadId, ActivityId activityId) {
        getLeadById.execute(leadId);
        return repository.findById(leadId, activityId)
                .orElseThrow(() -> new NotFoundException("error.activity.notFound", activityId));
    }
}
