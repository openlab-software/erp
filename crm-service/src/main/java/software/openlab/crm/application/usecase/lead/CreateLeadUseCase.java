package software.openlab.crm.application.usecase.lead;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.crm.application.usecase.stage.GetStageByIdUseCase;
import software.openlab.crm.domain.lead.Lead;
import software.openlab.crm.domain.lead.LeadCommand;
import software.openlab.crm.domain.lead.LeadEvents;
import software.openlab.crm.domain.lead.LeadRepository;
import software.openlab.crm.domain.lead.LeadType;
import software.openlab.crm.domain.shared.BadRequestException;
import software.openlab.crm.domain.shared.ConflictException;
import software.openlab.crm.domain.shared.DomainEvent;
import software.openlab.crm.domain.stage.Stage;
import software.openlab.crm.domain.stage.StageId;
import software.openlab.crm.domain.stage.StageRepository;

/** Creates a lead (or an opportunity) in the given stage, or in the first stage of the pipeline. */
@ApplicationScoped
public class CreateLeadUseCase {

    @Inject
    LeadRepository repository;

    @Inject
    StageRepository stages;

    @Inject
    GetStageByIdUseCase getStageById;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public Lead execute(String type, String stageId, LeadCommand raw) {
        LeadType leadType = type == null || type.isBlank() ? LeadType.OPPORTUNITY : LeadType.parse(type);
        LeadCommand command = raw.validated();

        Stage stage = stageId == null || stageId.isBlank()
                ? stages.findFirst().orElseThrow(() -> new ConflictException("error.stage.none"))
                : getStageById.execute(StageId.of(stageId));
        if (stage.isWon()) {
            throw new BadRequestException("error.lead.create.wonStage");
        }

        Lead lead = Lead.newLead(leadType, command, stage);
        repository.insert(lead);
        events.fire(new DomainEvent(LeadEvents.CREATED, LeadEvents.LeadPayload.of(lead)));
        return lead;
    }
}
