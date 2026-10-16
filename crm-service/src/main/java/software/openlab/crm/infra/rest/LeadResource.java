package software.openlab.crm.infra.rest;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import software.openlab.crm.application.usecase.lead.ConvertLeadToOpportunityUseCase;
import software.openlab.crm.application.usecase.lead.CreateLeadUseCase;
import software.openlab.crm.application.usecase.lead.DeleteLeadUseCase;
import software.openlab.crm.application.usecase.lead.GetLeadByIdUseCase;
import software.openlab.crm.application.usecase.lead.GetPipelineUseCase;
import software.openlab.crm.application.usecase.lead.ListLeadsUseCase;
import software.openlab.crm.application.usecase.lead.LoseLeadUseCase;
import software.openlab.crm.application.usecase.lead.MoveLeadStageUseCase;
import software.openlab.crm.application.usecase.lead.ReopenLeadUseCase;
import software.openlab.crm.application.usecase.lead.UpdateLeadUseCase;
import software.openlab.crm.application.usecase.lead.WinLeadUseCase;
import software.openlab.crm.domain.lead.LeadId;
import software.openlab.crm.domain.stage.StageId;
import software.openlab.crm.infra.rest.dto.LeadDTOs.ConvertRequest;
import software.openlab.crm.infra.rest.dto.LeadDTOs.LeadPageResponse;
import software.openlab.crm.infra.rest.dto.LeadDTOs.LeadRequest;
import software.openlab.crm.infra.rest.dto.LeadDTOs.LeadResponse;
import software.openlab.crm.infra.rest.dto.LeadDTOs.LoseRequest;
import software.openlab.crm.infra.rest.dto.LeadDTOs.MoveStageRequest;
import software.openlab.crm.infra.rest.dto.LeadDTOs.PipelineResponse;

@Path("/v1")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LeadResource {

    @Inject
    CreateLeadUseCase createLead;

    @Inject
    GetLeadByIdUseCase getLeadById;

    @Inject
    ListLeadsUseCase listLeads;

    @Inject
    UpdateLeadUseCase updateLead;

    @Inject
    DeleteLeadUseCase deleteLead;

    @Inject
    MoveLeadStageUseCase moveLeadStage;

    @Inject
    WinLeadUseCase winLead;

    @Inject
    LoseLeadUseCase loseLead;

    @Inject
    ReopenLeadUseCase reopenLead;

    @Inject
    ConvertLeadToOpportunityUseCase convertLead;

    @Inject
    GetPipelineUseCase getPipeline;

    @POST
    @Path("/leads")
    public Response create(@Valid LeadRequest request) {
        var created = createLead.execute(request.type(), request.stageId(), request.toCommand());
        return Response.status(Response.Status.CREATED).entity(LeadResponse.from(created)).build();
    }

    @GET
    @Path("/leads")
    public LeadPageResponse list(@QueryParam("q") String q,
                                 @QueryParam("type") String type,
                                 @QueryParam("status") String status,
                                 @QueryParam("stage_id") String stageId,
                                 @QueryParam("customer_id") String customerId,
                                 @QueryParam("assigned_to") String assignedTo,
                                 @QueryParam("tag") String tag,
                                 @QueryParam("page") Integer page,
                                 @QueryParam("page_size") Integer pageSize) {
        return LeadPageResponse.from(listLeads.execute(q, type, status, stageId, customerId, assignedTo, tag, page,
                pageSize));
    }

    @GET
    @Path("/leads/{id}")
    public LeadResponse getById(@PathParam("id") String id) {
        return LeadResponse.from(getLeadById.execute(LeadId.of(id)));
    }

    @PUT
    @Path("/leads/{id}")
    public LeadResponse update(@PathParam("id") String id, @Valid LeadRequest request) {
        return LeadResponse.from(updateLead.execute(LeadId.of(id), request.toCommand()));
    }

    @DELETE
    @Path("/leads/{id}")
    public Response delete(@PathParam("id") String id) {
        deleteLead.execute(LeadId.of(id));
        return Response.noContent().build();
    }

    @PATCH
    @Path("/leads/{id}/stage")
    public LeadResponse moveStage(@PathParam("id") String id, @Valid MoveStageRequest request) {
        return LeadResponse.from(moveLeadStage.execute(LeadId.of(id), StageId.of(request.stageId())));
    }

    @POST
    @Path("/leads/{id}/win")
    public LeadResponse win(@PathParam("id") String id) {
        return LeadResponse.from(winLead.execute(LeadId.of(id)));
    }

    @POST
    @Path("/leads/{id}/lose")
    public LeadResponse lose(@PathParam("id") String id, LoseRequest request) {
        return LeadResponse.from(loseLead.execute(LeadId.of(id), request == null ? null : request.lostReasonId()));
    }

    @POST
    @Path("/leads/{id}/reopen")
    public LeadResponse reopen(@PathParam("id") String id) {
        return LeadResponse.from(reopenLead.execute(LeadId.of(id)));
    }

    @POST
    @Path("/leads/{id}/convert")
    public LeadResponse convert(@PathParam("id") String id, ConvertRequest request) {
        return LeadResponse.from(convertLead.execute(LeadId.of(id), request == null ? null : request.customerId()));
    }

    @GET
    @Path("/pipeline")
    public PipelineResponse pipeline() {
        return PipelineResponse.from(getPipeline.execute());
    }
}
