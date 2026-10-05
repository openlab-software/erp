package software.openlab.stock.infra.rest;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import software.openlab.stock.application.usecase.movementreason.CreateMovementReasonUseCase;
import software.openlab.stock.application.usecase.movementreason.DeleteMovementReasonUseCase;
import software.openlab.stock.application.usecase.movementreason.ListMovementReasonsUseCase;
import software.openlab.stock.application.usecase.movementreason.UpdateMovementReasonUseCase;
import software.openlab.stock.domain.movementreason.MovementReason;
import software.openlab.stock.domain.movementreason.MovementReasonId;
import software.openlab.stock.domain.shared.PageResult;
import software.openlab.stock.infra.rest.dto.CreateMovementReasonRequest;
import software.openlab.stock.infra.rest.dto.ListingPageResponse;
import software.openlab.stock.infra.rest.dto.MovementReasonResponse;
import software.openlab.stock.infra.rest.dto.UpdateMovementReasonRequest;

@Path("/v1/movement-reasons")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MovementReasonResource {

    @Inject
    CreateMovementReasonUseCase createMovementReason;

    @Inject
    ListMovementReasonsUseCase listMovementReasons;

    @Inject
    UpdateMovementReasonUseCase updateMovementReason;

    @Inject
    DeleteMovementReasonUseCase deleteMovementReason;

    @GET
    public ListingPageResponse<MovementReasonResponse> list(@QueryParam("page") @DefaultValue("1") int page,
                                                              @QueryParam("page_size") @DefaultValue("20") int pageSize,
                                                              @QueryParam("q") String q) {
        PageResult<MovementReason> result = listMovementReasons.execute(q, page, pageSize);
        return new ListingPageResponse<>(
                result.data().stream().map(MovementReasonResponse::from).toList(),
                result.page(), result.pageSize(), result.total());
    }

    @POST
    public Response create(@Valid CreateMovementReasonRequest request) {
        MovementReasonResponse response = MovementReasonResponse.from(createMovementReason.execute(request.description()));
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @PUT
    @Path("/{id}")
    public MovementReasonResponse update(@PathParam("id") String id, @Valid UpdateMovementReasonRequest request) {
        return MovementReasonResponse.from(updateMovementReason.execute(MovementReasonId.of(id), request.description()));
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") String id) {
        deleteMovementReason.execute(MovementReasonId.of(id));
        return Response.noContent().build();
    }
}
