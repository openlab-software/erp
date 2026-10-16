package software.openlab.crm.domain.lead;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import software.openlab.crm.domain.shared.PageResult;
import software.openlab.crm.domain.stage.StageId;

public interface LeadRepository {

    Optional<Lead> findById(LeadId leadId);

    PageResult<Lead> find(LeadFilter filter, int page, int pageSize);

    /** Open leads aggregated per stage (for the pipeline board). */
    List<StageTotals> openTotalsByStage();

    void insert(Lead lead);

    void update(Lead lead);

    void deleteById(LeadId leadId);

    /** weightedRevenue is the sum of expected_revenue * probability / 100. */
    record StageTotals(StageId stageId, long count, BigDecimal expectedRevenue, BigDecimal weightedRevenue) {
    }
}
