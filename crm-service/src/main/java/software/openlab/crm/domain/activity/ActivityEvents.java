package software.openlab.crm.domain.activity;

import java.time.LocalDate;

public final class ActivityEvents {

    public static final String CREATED = "activity.created";
    public static final String DONE = "activity.done";

    private ActivityEvents() {
    }

    public record ActivityPayload(String id, String leadId, String type, String summary, LocalDate dueDate,
                                  String assignedTo, boolean done) {
        public static ActivityPayload of(Activity a) {
            return new ActivityPayload(a.getActivityId().toString(), a.getLeadId().toString(), a.getType().name(),
                    a.getSummary(), a.getDueDate(), a.getAssignedTo(), a.isDone());
        }
    }
}
