package software.openlab.crm.application.usecase.lead;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.crm.domain.lead.Lead;
import software.openlab.crm.domain.lead.LeadEvents;
import software.openlab.crm.domain.lead.LeadId;
import software.openlab.crm.domain.lead.LeadRepository;
import software.openlab.crm.domain.shared.DomainEvent;
import software.openlab.crm.domain.stage.StageRepository;

@ApplicationScoped
public class WinLeadUseCase {

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
        lead.win(stages.findWonStage().orElse(null));
        repository.update(lead);

        Lead updated = getLeadById.execute(leadId);
        events.fire(new DomainEvent(LeadEvents.WON, LeadEvents.LeadPayload.of(updated)));
        return updated;
    }
}
