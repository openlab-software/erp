package software.openlab.crm.application.usecase.lead;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.crm.domain.lead.LeadEvents;
import software.openlab.crm.domain.lead.LeadId;
import software.openlab.crm.domain.lead.LeadRepository;
import software.openlab.crm.domain.shared.DomainEvent;

@ApplicationScoped
public class DeleteLeadUseCase {

    @Inject
    LeadRepository repository;

    @Inject
    GetLeadByIdUseCase getLeadById;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public void execute(LeadId leadId) {
        getLeadById.execute(leadId);
        repository.deleteById(leadId);
        events.fire(new DomainEvent(LeadEvents.DELETED, new LeadEvents.LeadDeletedPayload(leadId.toString())));
    }
}
