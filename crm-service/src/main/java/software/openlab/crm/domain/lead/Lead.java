package software.openlab.crm.domain.lead;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.openlab.crm.domain.lostreason.LostReasonId;
import software.openlab.crm.domain.shared.ConflictException;
import software.openlab.crm.domain.stage.Stage;
import software.openlab.crm.domain.stage.StageId;

/**
 * A lead / opportunity of the pipeline. Open leads move between stages; closing one as WON or LOST freezes
 * it until it is reopened.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Lead {

    private LeadId leadId;
    private String name;
    private LeadType type;
    private LeadStatus status;
    private StageId stageId;
    /** Public id of a customer-service customer; optional. */
    private String customerId;
    private String contactName;
    private String companyName;
    private String email;
    private String phone;
    private BigDecimal expectedRevenue;
    /** Win probability, 0-100. Starts at the stage default and may be overridden by hand. */
    private int probability;
    /** 0 (normal) to 3 (very high). */
    private int priority;
    private LocalDate expectedClosing;
    private String description;
    private String assignedTo;
    private Set<String> tags;
    private LostReasonId lostReasonId;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant closedAt;

    public static Lead newLead(LeadType type, LeadCommand command, Stage stage) {
        Lead lead = new Lead();
        lead.leadId = LeadId.generate();
        lead.type = type;
        lead.status = LeadStatus.OPEN;
        lead.stageId = stage.getStageId();
        lead.probability = stage.getProbability();
        lead.createdAt = Instant.now();
        lead.apply(command);
        return lead;
    }

    /** Replaces every editable field (PUT semantics); stage, status and type change through their own operations. */
    public void apply(LeadCommand c) {
        this.name = c.name();
        this.customerId = c.customerId();
        this.contactName = c.contactName();
        this.companyName = c.companyName();
        this.email = c.email();
        this.phone = c.phone();
        this.expectedRevenue = c.expectedRevenue();
        if (c.probability() != null) {
            this.probability = c.probability();
        }
        this.priority = c.priority();
        this.expectedClosing = c.expectedClosing();
        this.description = c.description();
        this.assignedTo = c.assignedTo();
        this.tags = c.tags();
    }

    public boolean isOpen() {
        return status == LeadStatus.OPEN;
    }

    public void moveTo(Stage stage) {
        requireOpen();
        this.stageId = stage.getStageId();
        this.probability = stage.getProbability();
    }

    /** Closes the lead as won; it also lands in the pipeline's won stage when there is one. */
    public void win(Stage wonStage) {
        requireOpen();
        if (wonStage != null) {
            this.stageId = wonStage.getStageId();
        }
        this.status = LeadStatus.WON;
        this.probability = 100;
        this.type = LeadType.OPPORTUNITY;
        this.closedAt = Instant.now();
    }

    public void lose(LostReasonId reason) {
        requireOpen();
        this.status = LeadStatus.LOST;
        this.probability = 0;
        this.lostReasonId = reason;
        this.closedAt = Instant.now();
    }

    /** Back to OPEN in {@code stage} (normally the first one). */
    public void reopen(Stage stage) {
        if (isOpen()) {
            throw new ConflictException("error.lead.notClosed");
        }
        this.status = LeadStatus.OPEN;
        this.stageId = stage.getStageId();
        this.probability = stage.getProbability();
        this.lostReasonId = null;
        this.closedAt = null;
    }

    public void convertToOpportunity(String customerId) {
        requireOpen();
        if (type == LeadType.OPPORTUNITY) {
            throw new ConflictException("error.lead.alreadyOpportunity");
        }
        this.type = LeadType.OPPORTUNITY;
        if (customerId != null) {
            this.customerId = customerId;
        }
    }

    private void requireOpen() {
        if (!isOpen()) {
            throw new ConflictException("error.lead.closed", status);
        }
    }
}
