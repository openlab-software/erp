package software.openlab.crm.domain.activity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import software.openlab.crm.domain.lead.LeadId;
import software.openlab.crm.domain.shared.ConflictException;

class ActivityTest {

    @Test
    void anOpenActivityPastItsDueDateIsOverdueUntilItIsDone() {
        Activity activity = Activity.newActivity(LeadId.generate(), ActivityType.CALL, "Ligar", null,
                LocalDate.now().minusDays(1), null);

        assertTrue(activity.isOverdue(LocalDate.now()));
        activity.markDone();
        assertFalse(activity.isOverdue(LocalDate.now()));
        assertThrows(ConflictException.class, activity::markDone);
    }
}
