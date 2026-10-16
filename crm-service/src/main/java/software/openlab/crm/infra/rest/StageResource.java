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
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import software.openlab.crm.application.usecase.stage.CreateStageUseCase;
import software.openlab.crm.application.usecase.stage.DeleteStageUseCase;
import software.openlab.crm.application.usecase.stage.GetStageByIdUseCase;
import software.openlab.crm.application.usecase.stage.ListStagesUseCase;
import software.openlab.crm.application.usecase.stage.UpdateStageUseCase;
import software.openlab.crm.domain.stage.StageId;
import software.openlab.crm.infra.rest.dto.StageDTOs.CreateStageRequest;
import software.openlab.crm.infra.rest.dto.StageDTOs.StageListResponse;
import software.openlab.crm.infra.rest.dto.StageDTOs.StageResponse;
import software.openlab.crm.infra.rest.dto.StageDTOs.UpdateStageRequest;

@Path("/v1/stages")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class StageResource {

    @Inject
    ListStagesUseCase listStages;

    @Inject
    GetStageByIdUseCase getStageById;

    @Inject
    CreateStageUseCase createStage;

    @Inject
    UpdateStageUseCase updateStage;

    @Inject
    DeleteStageUseCase deleteStage;

    @GET
    public StageListResponse list() {
        return StageListResponse.from(listStages.execute());
    }

    @GET
    @Path("/{id}")
    public StageResponse getById(@PathParam("id") String id) {
        return StageResponse.from(getStageById.execute(StageId.of(id)));
    }

    @POST
    public Response create(@Valid CreateStageRequest request) {
        var created = createStage.execute(request.name(), request.position(), request.probability(), request.won());
        return Response.status(Response.Status.CREATED).entity(StageResponse.from(created)).build();
    }

    @PUT
    @Path("/{id}")
    public StageResponse update(@PathParam("id") String id, @Valid UpdateStageRequest request) {
        return StageResponse.from(updateStage.execute(StageId.of(id), request.name(), request.position(),
                request.probability(), request.won()));
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") String id) {
        deleteStage.execute(StageId.of(id));
        return Response.noContent().build();
    }
}
