package software.openlab.crm.application.usecase.lead;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.crm.application.usecase.stage.GetStageByIdUseCase;
import software.openlab.crm.domain.lead.Lead;
import software.openlab.crm.domain.lead.LeadEvents;
import software.openlab.crm.domain.lead.LeadId;
import software.openlab.crm.domain.lead.LeadRepository;
import software.openlab.crm.domain.shared.DomainEvent;
import software.openlab.crm.domain.stage.Stage;
import software.openlab.crm.domain.stage.StageId;

/** Drags a lead to another column. Entering a "won" stage closes the lead as won. */
@ApplicationScoped
public class MoveLeadStageUseCase {

    @Inject
    LeadRepository repository;

    @Inject
    GetLeadByIdUseCase getLeadById;

    @Inject
    GetStageByIdUseCase getStageById;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public Lead execute(LeadId leadId, StageId stageId) {
        Lead lead = getLeadById.execute(leadId);
        Stage stage = getStageById.execute(stageId);

        String event;
        if (stage.isWon()) {
            lead.win(stage);
            event = LeadEvents.WON;
        } else {
            lead.moveTo(stage);
            event = LeadEvents.STAGE_CHANGED;
        }
        repository.update(lead);

        Lead updated = getLeadById.execute(leadId);
        events.fire(new DomainEvent(event, LeadEvents.LeadPayload.of(updated)));
        return updated;
    }
}
