package software.openlab.crm.application.usecase.lead;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.crm.domain.lead.Lead;
import software.openlab.crm.domain.lead.LeadEvents;
import software.openlab.crm.domain.lead.LeadId;
import software.openlab.crm.domain.lead.LeadRepository;
import software.openlab.crm.domain.shared.ConflictException;
import software.openlab.crm.domain.shared.DomainEvent;
import software.openlab.crm.domain.stage.StageRepository;

/** Puts a won/lost lead back at the start of the pipeline. */
@ApplicationScoped
public class ReopenLeadUseCase {

    @Inject
    LeadRepository repository;

    @Inject
    StageRepository stages;

    @Inject
    GetLeadByIdUseCase getLeadById;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public Lead execute(LeadId leadId) {
        Lead lead = getLeadById.execute(leadId);
        lead.reopen(stages.findFirst().orElseThrow(() -> new ConflictException("error.stage.none")));
        repository.update(lead);

        Lead updated = getLeadById.execute(leadId);
        events.fire(new DomainEvent(LeadEvents.REOPENED, LeadEvents.LeadPayload.of(updated)));
        return updated;
    }
}
