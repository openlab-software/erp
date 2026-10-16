package software.openlab.crm.infra.rest;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDate;
import software.openlab.crm.application.usecase.activity.AddActivityUseCase;
import software.openlab.crm.application.usecase.activity.DeleteActivityUseCase;
import software.openlab.crm.application.usecase.activity.ListAgendaUseCase;
import software.openlab.crm.application.usecase.activity.ListLeadActivitiesUseCase;
import software.openlab.crm.application.usecase.activity.MarkActivityDoneUseCase;
import software.openlab.crm.application.usecase.activity.UpdateActivityUseCase;
import software.openlab.crm.domain.activity.ActivityId;
import software.openlab.crm.domain.lead.LeadId;
import software.openlab.crm.infra.rest.dto.ActivityDTOs.ActivityListResponse;
import software.openlab.crm.infra.rest.dto.ActivityDTOs.ActivityPageResponse;
import software.openlab.crm.infra.rest.dto.ActivityDTOs.ActivityRequest;
import software.openlab.crm.infra.rest.dto.ActivityDTOs.ActivityResponse;

/** Follow-ups: nested under a lead, plus the cross-lead agenda at {@code /v1/activities}. */
@Path("/v1")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ActivityResource {

    @Inject
    ListLeadActivitiesUseCase listLeadActivities;

    @Inject
    AddActivityUseCase addActivity;

    @Inject
    UpdateActivityUseCase updateActivity;

    @Inject
    MarkActivityDoneUseCase markActivityDone;

    @Inject
    DeleteActivityUseCase deleteActivity;

    @Inject
    ListAgendaUseCase listAgenda;

    @GET
    @Path("/leads/{leadId}/activities")
    public ActivityListResponse list(@PathParam("leadId") String leadId) {
        return ActivityListResponse.from(listLeadActivities.execute(LeadId.of(leadId)));
    }

    @POST
    @Path("/leads/{leadId}/activities")
    public Response create(@PathParam("leadId") String leadId, @Valid ActivityRequest request) {
        var created = addActivity.execute(LeadId.of(leadId), request.type(), request.summary(), request.note(),
                request.dueDate(), request.assignedTo());
        return Response.status(Response.Status.CREATED).entity(ActivityResponse.from(created)).build();
    }

    @PUT
    @Path("/leads/{leadId}/activities/{activityId}")
    public ActivityResponse update(@PathParam("leadId") String leadId, @PathParam("activityId") String activityId,
                                   @Valid ActivityRequest request) {
        return ActivityResponse.from(updateActivity.execute(LeadId.of(leadId), ActivityId.of(activityId),
                request.type(), request.summary(), request.note(), request.dueDate(), request.assignedTo()));
    }

    @POST
    @Path("/leads/{leadId}/activities/{activityId}/done")
    public ActivityResponse done(@PathParam("leadId") String leadId, @PathParam("activityId") String activityId) {
        return ActivityResponse.from(markActivityDone.execute(LeadId.of(leadId), ActivityId.of(activityId)));
    }

    @DELETE
    @Path("/leads/{leadId}/activities/{activityId}")
    public Response delete(@PathParam("leadId") String leadId, @PathParam("activityId") String activityId) {
        deleteActivity.execute(LeadId.of(leadId), ActivityId.of(activityId));
        return Response.noContent().build();
    }

    @GET
    @Path("/activities")
    public ActivityPageResponse agenda(@QueryParam("done") Boolean done,
                                       @QueryParam("due_before") LocalDate dueBefore,
                                       @QueryParam("assigned_to") String assignedTo,
                                       @QueryParam("page") Integer page,
                                       @QueryParam("page_size") Integer pageSize) {
        return ActivityPageResponse.from(listAgenda.execute(done, dueBefore, assignedTo, page, pageSize));
    }
}
