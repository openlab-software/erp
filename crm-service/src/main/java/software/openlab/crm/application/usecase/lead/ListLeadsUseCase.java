package software.openlab.crm.application.usecase.lead;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.openlab.crm.domain.lead.Lead;
import software.openlab.crm.domain.lead.LeadFilter;
import software.openlab.crm.domain.lead.LeadRepository;
import software.openlab.crm.domain.lead.LeadStatus;
import software.openlab.crm.domain.lead.LeadType;
import software.openlab.crm.domain.shared.PageResult;
import software.openlab.crm.domain.shared.Refs;
import software.openlab.crm.domain.stage.StageId;

@ApplicationScoped
public class ListLeadsUseCase {

    @Inject
    LeadRepository repository;

    public PageResult<Lead> execute(String q, String type, String status, String stageId, String customerId,
                                    String assignedTo, String tag, Integer page, Integer pageSize) {
        LeadFilter filter = new LeadFilter(
                q,
                isBlank(type) ? null : LeadType.parse(type),
                isBlank(status) ? null : LeadStatus.parse(status),
                isBlank(stageId) ? null : StageId.of(stageId),
                Refs.optional("customer", customerId),
                assignedTo,
                tag);
        return repository.find(filter, PageResult.normalizePage(page), PageResult.normalizePageSize(pageSize));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
