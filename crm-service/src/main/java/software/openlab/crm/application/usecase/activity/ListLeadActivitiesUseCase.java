package software.openlab.crm.application.usecase.activity;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import software.openlab.crm.application.usecase.lead.GetLeadByIdUseCase;
import software.openlab.crm.domain.activity.Activity;
import software.openlab.crm.domain.activity.ActivityRepository;
import software.openlab.crm.domain.lead.LeadId;

@ApplicationScoped
public class ListLeadActivitiesUseCase {

    @Inject
    ActivityRepository repository;

    @Inject
    GetLeadByIdUseCase getLeadById;

    public List<Activity> execute(LeadId leadId) {
        getLeadById.execute(leadId);
        return repository.findByLead(leadId);
    }
}
