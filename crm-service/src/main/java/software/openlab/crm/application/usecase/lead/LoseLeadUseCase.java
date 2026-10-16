package software.openlab.crm.application.usecase.lead;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.crm.application.usecase.lostreason.GetLostReasonByIdUseCase;
import software.openlab.crm.domain.lead.Lead;
import software.openlab.crm.domain.lead.LeadEvents;
import software.openlab.crm.domain.lead.LeadId;
import software.openlab.crm.domain.lead.LeadRepository;
import software.openlab.crm.domain.lostreason.LostReason;
import software.openlab.crm.domain.lostreason.LostReasonId;
import software.openlab.crm.domain.shared.ConflictException;
import software.openlab.crm.domain.shared.DomainEvent;

@ApplicationScoped
public class LoseLeadUseCase {

    @Inject
    LeadRepository repository;

    @Inject
    GetLeadByIdUseCase getLeadById;

    @Inject
    GetLostReasonByIdUseCase getLostReasonById;

    @Inject
    Event<DomainEvent> events;

    /** {@code lostReasonId} is optional; when informed the reason must exist and be active. */
    @Transactional
    public Lead execute(LeadId leadId, String lostReasonId) {
        Lead lead = getLeadById.execute(leadId);

        LostReasonId reasonId = null;
        if (lostReasonId != null && !lostReasonId.isBlank()) {
            reasonId = LostReasonId.of(lostReasonId);
            LostReason reason = getLostReasonById.execute(reasonId);
            if (!reason.isActive()) {
                throw new ConflictException("error.lostReason.inactive", reason.getName());
            }
        }

        lead.lose(reasonId);
        repository.update(lead);

        Lead updated = getLeadById.execute(leadId);
        events.fire(new DomainEvent(LeadEvents.LOST, LeadEvents.LeadPayload.of(updated)));
        return updated;
    }
}
