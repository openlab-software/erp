package software.openlab.crm.infra.rest.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import software.openlab.crm.application.usecase.lead.GetPipelineUseCase.Column;
import software.openlab.crm.application.usecase.lead.GetPipelineUseCase.Pipeline;
import software.openlab.crm.domain.lead.Lead;
import software.openlab.crm.domain.lead.LeadCommand;
import software.openlab.crm.domain.shared.PageResult;

public final class LeadDTOs {

    private LeadDTOs() {
    }

    /**
     * {@code type} is LEAD or OPPORTUNITY (default OPPORTUNITY); {@code stageId} defaults to the first stage.
     * Both are only read on creation.
     */
    public record LeadRequest(
            @NotBlank String name,
            String type,
            String stageId,
            String customerId,
            String contactName,
            String companyName,
            String email,
            String phone,
            @PositiveOrZero BigDecimal expectedRevenue,
            @Min(0) @Max(100) Integer probability,
            @Min(0) @Max(3) Integer priority,
            LocalDate expectedClosing,
            String description,
            String assignedTo,
            Set<String> tags
    ) {
        public LeadCommand toCommand() {
            return new LeadCommand(name, customerId, contactName, companyName, email, phone, expectedRevenue,
                    probability, priority == null ? 0 : priority, expectedClosing, description, assignedTo, tags);
        }
    }

    public record MoveStageRequest(@NotBlank String stageId) {
    }

    public record LoseRequest(String lostReasonId) {
    }

    public record ConvertRequest(String customerId) {
    }

    public record LeadResponse(
            String leadId,
            String name,
            String type,
            String status,
            String stageId,
            String customerId,
            String contactName,
            String companyName,
            String email,
            String phone,
            BigDecimal expectedRevenue,
            int probability,
            int priority,
            LocalDate expectedClosing,
            String description,
            String assignedTo,
            Set<String> tags,
            String lostReasonId,
            Instant createdAt,
            Instant updatedAt,
            Instant closedAt
    ) {
        public static LeadResponse from(Lead l) {
            return new LeadResponse(l.getLeadId().toString(), l.getName(), l.getType().name(), l.getStatus().name(),
                    l.getStageId().toString(), l.getCustomerId(), l.getContactName(), l.getCompanyName(),
                    l.getEmail(), l.getPhone(), l.getExpectedRevenue(), l.getProbability(), l.getPriority(),
                    l.getExpectedClosing(), l.getDescription(), l.getAssignedTo(), l.getTags(),
                    l.getLostReasonId() == null ? null : l.getLostReasonId().toString(), l.getCreatedAt(),
                    l.getUpdatedAt(), l.getClosedAt());
        }
    }

    public record LeadPageResponse(List<LeadResponse> data, int page, int pageSize, long total) {
        public static LeadPageResponse from(PageResult<Lead> page) {
            return new LeadPageResponse(page.data().stream().map(LeadResponse::from).toList(), page.page(),
                    page.pageSize(), page.total());
        }
    }

    public record PipelineColumnResponse(String stageId, String name, int position, int probability, boolean won,
                                         long count, BigDecimal expectedRevenue, BigDecimal weightedRevenue) {
        static PipelineColumnResponse from(Column c) {
            return new PipelineColumnResponse(c.stage().getStageId().toString(), c.stage().getName(),
                    c.stage().getPosition(), c.stage().getProbability(), c.stage().isWon(), c.count(),
                    c.expectedRevenue(), c.weightedRevenue());
        }
    }

    /** The forecast: open leads per stage and in total ({@code weighted_revenue} = revenue x probability). */
    public record PipelineResponse(List<PipelineColumnResponse> columns, long count, BigDecimal expectedRevenue,
                                   BigDecimal weightedRevenue) {
        public static PipelineResponse from(Pipeline p) {
            return new PipelineResponse(p.columns().stream().map(PipelineColumnResponse::from).toList(), p.count(),
                    p.expectedRevenue(), p.weightedRevenue());
        }
    }
}
