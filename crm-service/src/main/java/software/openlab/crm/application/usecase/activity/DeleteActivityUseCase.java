package software.openlab.crm.application.usecase.activity;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.crm.domain.activity.ActivityId;
import software.openlab.crm.domain.activity.ActivityRepository;
import software.openlab.crm.domain.lead.LeadId;

@ApplicationScoped
public class DeleteActivityUseCase {

    @Inject
    ActivityRepository repository;

    @Inject
    GetActivityUseCase getActivity;

    @Transactional
    public void execute(LeadId leadId, ActivityId activityId) {
        getActivity.execute(leadId, activityId);
        repository.deleteById(leadId, activityId);
    }
}
