package software.openlab.crm.application.usecase.lead;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import software.openlab.crm.domain.lead.LeadRepository;
import software.openlab.crm.domain.lead.LeadRepository.StageTotals;
import software.openlab.crm.domain.stage.Stage;
import software.openlab.crm.domain.stage.StageId;
import software.openlab.crm.domain.stage.StageRepository;

/** Open-lead totals per stage, in pipeline order, plus the grand total (the forecast). */
@ApplicationScoped
public class GetPipelineUseCase {

    @Inject
    LeadRepository leads;

    @Inject
    StageRepository stages;

    public Pipeline execute() {
        Map<StageId, StageTotals> totals = leads.openTotalsByStage().stream()
                .collect(Collectors.toMap(StageTotals::stageId, Function.identity()));

        List<Column> columns = stages.findAll().stream().map(stage -> {
            StageTotals t = totals.get(stage.getStageId());
            return t == null
                    ? new Column(stage, 0, BigDecimal.ZERO, BigDecimal.ZERO)
                    : new Column(stage, t.count(), t.expectedRevenue(), t.weightedRevenue());
        }).toList();

        long count = columns.stream().mapToLong(Column::count).sum();
        BigDecimal expected = columns.stream().map(Column::expectedRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal weighted = columns.stream().map(Column::weightedRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Pipeline(columns, count, expected, weighted);
    }

    public record Column(Stage stage, long count, BigDecimal expectedRevenue, BigDecimal weightedRevenue) {
    }

    public record Pipeline(List<Column> columns, long count, BigDecimal expectedRevenue, BigDecimal weightedRevenue) {
    }
}
