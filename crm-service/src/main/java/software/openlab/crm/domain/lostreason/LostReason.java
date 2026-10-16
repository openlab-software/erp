package software.openlab.crm.domain.lostreason;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Why an opportunity was lost ("Too expensive", "No budget", ...), picked when marking a lead as lost. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LostReason {

    private LostReasonId lostReasonId;
    private String name;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public static LostReason newReason(String name) {
        return new LostReason(LostReasonId.generate(), name, true, Instant.now(), null);
    }
}
