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
import software.openlab.crm.application.usecase.lostreason.CreateLostReasonUseCase;
import software.openlab.crm.application.usecase.lostreason.DeleteLostReasonUseCase;
import software.openlab.crm.application.usecase.lostreason.ListLostReasonsUseCase;
import software.openlab.crm.application.usecase.lostreason.UpdateLostReasonUseCase;
import software.openlab.crm.domain.lostreason.LostReasonId;
import software.openlab.crm.infra.rest.dto.LostReasonDTOs.CreateLostReasonRequest;
import software.openlab.crm.infra.rest.dto.LostReasonDTOs.LostReasonListResponse;
import software.openlab.crm.infra.rest.dto.LostReasonDTOs.LostReasonResponse;
import software.openlab.crm.infra.rest.dto.LostReasonDTOs.UpdateLostReasonRequest;

@Path("/v1/lost-reasons")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LostReasonResource {

    @Inject
    ListLostReasonsUseCase listLostReasons;

    @Inject
    CreateLostReasonUseCase createLostReason;

    @Inject
    UpdateLostReasonUseCase updateLostReason;

    @Inject
    DeleteLostReasonUseCase deleteLostReason;

    @GET
    public LostReasonListResponse list() {
        return LostReasonListResponse.from(listLostReasons.execute());
    }

    @POST
    public Response create(@Valid CreateLostReasonRequest request) {
        var created = createLostReason.execute(request.name());
        return Response.status(Response.Status.CREATED).entity(LostReasonResponse.from(created)).build();
    }

    @PUT
    @Path("/{id}")
    public LostReasonResponse update(@PathParam("id") String id, @Valid UpdateLostReasonRequest request) {
        return LostReasonResponse.from(updateLostReason.execute(LostReasonId.of(id), request.name(),
                request.active()));
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") String id) {
        deleteLostReason.execute(LostReasonId.of(id));
        return Response.noContent().build();
    }
}
