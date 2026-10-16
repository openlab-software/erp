package software.openlab.crm.domain.activity;

import java.time.Instant;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.openlab.crm.domain.lead.LeadId;
import software.openlab.crm.domain.shared.ConflictException;
import software.openlab.crm.domain.shared.Fields;

/** A scheduled follow-up (call, meeting, e-mail, to-do) attached to a lead. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Activity {

    private ActivityId activityId;
    private LeadId leadId;
    private ActivityType type;
    private String summary;
    private String note;
    private LocalDate dueDate;
    private String assignedTo;
    private boolean done;
    private Instant doneAt;
    private Instant createdAt;

    public static Activity newActivity(LeadId leadId, ActivityType type, String summary, String note,
                                       LocalDate dueDate, String assignedTo) {
        Activity a = new Activity(ActivityId.generate(), leadId, type, null, null, null, null, false, null,
                Instant.now());
        a.apply(type, summary, note, dueDate, assignedTo);
        return a;
    }

    public void apply(ActivityType type, String summary, String note, LocalDate dueDate, String assignedTo) {
        this.type = type;
        this.summary = Fields.requireNonBlank(summary, "summary");
        this.note = note == null || note.isBlank() ? null : note.trim();
        this.dueDate = dueDate;
        this.assignedTo = assignedTo == null || assignedTo.isBlank() ? null : assignedTo.trim();
    }

    public void markDone() {
        if (done) {
            throw new ConflictException("error.activity.alreadyDone");
        }
        this.done = true;
        this.doneAt = Instant.now();
    }

    public boolean isOverdue(LocalDate today) {
        return !done && dueDate != null && dueDate.isBefore(today);
    }
}
