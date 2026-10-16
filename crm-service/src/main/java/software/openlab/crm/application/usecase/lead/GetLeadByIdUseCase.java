package software.openlab.crm.application.usecase.lead;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.openlab.crm.domain.lead.Lead;
import software.openlab.crm.domain.lead.LeadId;
import software.openlab.crm.domain.lead.LeadRepository;
import software.openlab.crm.domain.shared.NotFoundException;

@ApplicationScoped
public class GetLeadByIdUseCase {

    @Inject
    LeadRepository repository;

    public Lead execute(LeadId leadId) {
        return repository.findById(leadId).orElseThrow(() -> new NotFoundException("error.lead.notFound", leadId));
    }
}
