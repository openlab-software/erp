package software.openlab.crm.domain.lead;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.Test;
import software.openlab.crm.domain.lostreason.LostReasonId;
import software.openlab.crm.domain.shared.BadRequestException;
import software.openlab.crm.domain.shared.ConflictException;
import software.openlab.crm.domain.stage.Stage;

class LeadTest {

    private static final Stage NEW = Stage.newStage("Novo", 10, 10, false);
    private static final Stage PROPOSAL = Stage.newStage("Proposta", 30, 60, false);
    private static final Stage WON = Stage.newStage("Ganho", 40, 0, true);

    private static LeadCommand command(String name) {
        return new LeadCommand(name, null, "Ana", "ACME", "ana@acme.com", null, new BigDecimal("1000"), null, 1, null,
                null, "joao", Set.of(" VIP ", "")).validated();
    }

    @Test
    void newLeadStartsOpenWithTheStageProbabilityAndNormalizedData() {
        Lead lead = Lead.newLead(LeadType.OPPORTUNITY, command("Venda ACME"), NEW);

        assertEquals(LeadStatus.OPEN, lead.getStatus());
        assertEquals(10, lead.getProbability());
        assertEquals(Set.of("vip"), lead.getTags());
        assertEquals(new BigDecimal("1000.00"), lead.getExpectedRevenue());
    }

    @Test
    void movingBetweenStagesTakesTheStageProbability() {
        Lead lead = Lead.newLead(LeadType.OPPORTUNITY, command("x"), NEW);
        lead.moveTo(PROPOSAL);

        assertEquals(PROPOSAL.getStageId(), lead.getStageId());
        assertEquals(60, lead.getProbability());
    }

    @Test
    void winningClosesTheLeadInTheWonStage() {
        Lead lead = Lead.newLead(LeadType.LEAD, command("x"), NEW);
        lead.win(WON);

        assertEquals(LeadStatus.WON, lead.getStatus());
        assertEquals(100, lead.getProbability());
        assertEquals(LeadType.OPPORTUNITY, lead.getType());
        assertEquals(WON.getStageId(), lead.getStageId());
    }

    @Test
    void aClosedLeadCannotChangeStageOrBeClosedAgainUntilReopened() {
        Lead lead = Lead.newLead(LeadType.OPPORTUNITY, command("x"), NEW);
        lead.lose(LostReasonId.generate());

        assertEquals(0, lead.getProbability());
        assertThrows(ConflictException.class, () -> lead.moveTo(PROPOSAL));
        assertThrows(ConflictException.class, () -> lead.win(WON));

        lead.reopen(NEW);
        assertTrue(lead.isOpen());
        assertNull(lead.getLostReasonId());
        assertNull(lead.getClosedAt());
    }

    @Test
    void reopeningAnOpenLeadIsAConflict() {
        Lead lead = Lead.newLead(LeadType.OPPORTUNITY, command("x"), NEW);
        assertThrows(ConflictException.class, () -> lead.reopen(NEW));
    }

    @Test
    void convertingToOpportunityLinksTheCustomerOnce() {
        Lead lead = Lead.newLead(LeadType.LEAD, command("x"), NEW);
        String customer = "customer_" + software.openlab.crm.domain.shared.Ulid.generate();

        lead.convertToOpportunity(customer);

        assertEquals(LeadType.OPPORTUNITY, lead.getType());
        assertEquals(customer, lead.getCustomerId());
        assertThrows(ConflictException.class, () -> lead.convertToOpportunity(null));
    }

    @Test
    void commandRejectsBadEmailPriorityAndCustomerReference() {
        assertThrows(BadRequestException.class, () -> new LeadCommand("x", null, null, null, "not-an-email", null,
                null, null, 0, null, null, null, null).validated());
        assertThrows(BadRequestException.class, () -> new LeadCommand("x", null, null, null, null, null, null, null,
                9, null, null, null, null).validated());
        assertThrows(BadRequestException.class, () -> new LeadCommand("x", "supplier_01", null, null, null, null,
                null, null, 0, null, null, null, null).validated());
        assertThrows(BadRequestException.class, () -> new LeadCommand("  ", null, null, null, null, null, null, null,
                0, null, null, null, null).validated());
    }
}
