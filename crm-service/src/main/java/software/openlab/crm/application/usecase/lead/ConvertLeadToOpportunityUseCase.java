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
import software.openlab.crm.domain.shared.Refs;

/** Qualifies a lead as an opportunity, optionally linking it to an existing customer. */
@ApplicationScoped
public class ConvertLeadToOpportunityUseCase {

    @Inject
    LeadRepository repository;

    @Inject
    GetLeadByIdUseCase getLeadById;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public Lead execute(LeadId leadId, String customerId) {
        Lead lead = getLeadById.execute(leadId);
        lead.convertToOpportunity(Refs.optional("customer", customerId));
        repository.update(lead);

        Lead updated = getLeadById.execute(leadId);
        events.fire(new DomainEvent(LeadEvents.UPDATED, LeadEvents.LeadPayload.of(updated)));
        return updated;
    }
}
