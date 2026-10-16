package software.openlab.crm.infra.rest.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import software.openlab.crm.domain.activity.Activity;
import software.openlab.crm.domain.shared.PageResult;

public final class ActivityDTOs {

    private ActivityDTOs() {
    }

    /** {@code type} is TODO (default), CALL, MEETING or EMAIL. */
    public record ActivityRequest(String type, @NotBlank String summary, String note, LocalDate dueDate,
                                  String assignedTo) {
    }

    public record ActivityResponse(String activityId, String leadId, String type, String summary, String note,
                                   LocalDate dueDate, String assignedTo, boolean done, boolean overdue,
                                   Instant doneAt, Instant createdAt) {
        public static ActivityResponse from(Activity a) {
            return new ActivityResponse(a.getActivityId().toString(), a.getLeadId().toString(), a.getType().name(),
                    a.getSummary(), a.getNote(), a.getDueDate(), a.getAssignedTo(), a.isDone(),
                    a.isOverdue(LocalDate.now()), a.getDoneAt(), a.getCreatedAt());
        }
    }

    /** Not paginated: the activities of one lead. */
    public record ActivityListResponse(List<ActivityResponse> data) {
        public static ActivityListResponse from(List<Activity> activities) {
            return new ActivityListResponse(activities.stream().map(ActivityResponse::from).toList());
        }
    }

    public record ActivityPageResponse(List<ActivityResponse> data, int page, int pageSize, long total) {
        public static ActivityPageResponse from(PageResult<Activity> page) {
            return new ActivityPageResponse(page.data().stream().map(ActivityResponse::from).toList(), page.page(),
                    page.pageSize(), page.total());
        }
    }
}
