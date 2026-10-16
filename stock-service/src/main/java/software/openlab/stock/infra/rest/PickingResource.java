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
import software.openlab.stock.application.usecase.picking.CancelPickingUseCase;
import software.openlab.stock.application.usecase.picking.ConfirmPickingUseCase;
import software.openlab.stock.application.usecase.picking.CreatePickingUseCase;
import software.openlab.stock.application.usecase.picking.DeletePickingUseCase;
import software.openlab.stock.application.usecase.picking.GetPickingByIdUseCase;
import software.openlab.stock.application.usecase.picking.ListPickingsUseCase;
import software.openlab.stock.application.usecase.picking.ListReplenishmentUseCase;
import software.openlab.stock.application.usecase.picking.UpdatePickingUseCase;
import software.openlab.stock.application.usecase.picking.ValidatePickingUseCase;
import software.openlab.stock.domain.picking.PickingId;
import software.openlab.stock.infra.rest.dto.ListingPageResponse;
import software.openlab.stock.infra.rest.dto.PickingDTOs;
import software.openlab.stock.infra.rest.dto.PickingDTOs.PickingRequest;
import software.openlab.stock.infra.rest.dto.PickingDTOs.PickingResponse;
import software.openlab.stock.infra.rest.dto.PickingDTOs.ReplenishmentResponse;
import software.openlab.stock.infra.rest.dto.PickingDTOs.ValidateRequest;
import software.openlab.stock.infra.rest.dto.PickingDTOs.ValidateResponse;

/** Receipts, deliveries and internal transfers, plus the reordering-rule report at {@code /v1/replenishment}. */
@Path("/v1")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PickingResource {

    @Inject
    CreatePickingUseCase createPicking;

    @Inject
    GetPickingByIdUseCase getPickingById;

    @Inject
    ListPickingsUseCase listPickings;

    @Inject
    UpdatePickingUseCase updatePicking;

    @Inject
    DeletePickingUseCase deletePicking;

    @Inject
    ConfirmPickingUseCase confirmPicking;

    @Inject
    ValidatePickingUseCase validatePicking;

    @Inject
    CancelPickingUseCase cancelPicking;

    @Inject
    ListReplenishmentUseCase listReplenishment;

    @POST
    @Path("/pickings")
    public Response create(@Valid PickingRequest request) {
        var created = createPicking.execute(request.type() == null ? "" : request.type(), request.toCommand());
        return Response.status(Response.Status.CREATED).entity(PickingResponse.from(created)).build();
    }

    @GET
    @Path("/pickings")
    public ListingPageResponse<PickingResponse> list(@QueryParam("q") String q,
                                                     @QueryParam("type") String type,
                                                     @QueryParam("state") String state,
                                                     @QueryParam("origin_type") String originType,
                                                     @QueryParam("origin_id") String originId,
                                                     @QueryParam("stock_id") String stockId,
                                                     @QueryParam("page") @DefaultValue("1") int page,
                                                     @QueryParam("page_size") @DefaultValue("20") int pageSize) {
        return PickingDTOs.page(listPickings.execute(q, type, state, originType, originId, stockId, page,
                pageSize));
    }

    @GET
    @Path("/pickings/{id}")
    public PickingResponse getById(@PathParam("id") String id) {
        return PickingResponse.from(getPickingById.execute(PickingId.of(id)));
    }

    @PUT
    @Path("/pickings/{id}")
    public PickingResponse update(@PathParam("id") String id, @Valid PickingRequest request) {
        return PickingResponse.from(updatePicking.execute(PickingId.of(id), request.toCommand()));
    }

    @DELETE
    @Path("/pickings/{id}")
    public Response delete(@PathParam("id") String id) {
        deletePicking.execute(PickingId.of(id));
        return Response.noContent().build();
    }

    @POST
    @Path("/pickings/{id}/confirm")
    public PickingResponse confirm(@PathParam("id") String id) {
        return PickingResponse.from(confirmPicking.execute(PickingId.of(id)));
    }

    @POST
    @Path("/pickings/{id}/validate")
    public ValidateResponse validate(@PathParam("id") String id, @Valid ValidateRequest request) {
        ValidateRequest body = request == null ? new ValidateRequest(null, null) : request;
        return ValidateResponse.from(validatePicking.execute(PickingId.of(id), body.doneByProduct(),
                body.backorder()));
    }

    @POST
    @Path("/pickings/{id}/cancel")
    public PickingResponse cancel(@PathParam("id") String id) {
        return PickingResponse.from(cancelPicking.execute(PickingId.of(id)));
    }

    @GET
    @Path("/replenishment")
    public ListingPageResponse<ReplenishmentResponse> replenishment(
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("page_size") @DefaultValue("20") int pageSize) {
        return PickingDTOs.replenishmentPage(listReplenishment.execute(page, pageSize));
    }
}
