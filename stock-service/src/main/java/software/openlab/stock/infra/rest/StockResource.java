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
import java.time.Instant;
import java.time.format.DateTimeParseException;
import software.openlab.stock.application.usecase.movement.CreateStockMovementUseCase;
import software.openlab.stock.application.usecase.movement.ListStockMovementsUseCase;
import software.openlab.stock.application.usecase.stock.CreateStockUseCase;
import software.openlab.stock.application.usecase.stock.DeleteStockUseCase;
import software.openlab.stock.application.usecase.stock.GetStockByIdUseCase;
import software.openlab.stock.application.usecase.stock.ListStocksUseCase;
import software.openlab.stock.application.usecase.stock.UpdateStockUseCase;
import software.openlab.stock.application.usecase.stockcount.CreateStockCountUseCase;
import software.openlab.stock.application.usecase.stockcount.ListStockCountsUseCase;
import software.openlab.stock.application.usecase.stockitem.GetStockItemUseCase;
import software.openlab.stock.application.usecase.stockitem.ListStockItemsUseCase;
import software.openlab.stock.application.usecase.stockitem.ReleaseStockUseCase;
import software.openlab.stock.application.usecase.stockitem.ReserveStockUseCase;
import software.openlab.stock.domain.movement.StockMovement;
import software.openlab.stock.domain.movement.StockMovementType;
import software.openlab.stock.domain.shared.BadRequestException;
import software.openlab.stock.domain.shared.PageResult;
import software.openlab.stock.domain.shared.ProductId;
import software.openlab.stock.domain.stock.Stock;
import software.openlab.stock.domain.stock.StockId;
import software.openlab.stock.domain.stockcount.StockCount;
import software.openlab.stock.domain.stockitem.StockItem;
import software.openlab.stock.infra.rest.dto.CreateStockCountRequest;
import software.openlab.stock.infra.rest.dto.CreateStockMovementRequest;
import software.openlab.stock.infra.rest.dto.CreateStockRequest;
import software.openlab.stock.infra.rest.dto.ListingPageResponse;
import software.openlab.stock.infra.rest.dto.QuantityRequest;
import software.openlab.stock.infra.rest.dto.StockCountResponse;
import software.openlab.stock.infra.rest.dto.StockItemResponse;
import software.openlab.stock.infra.rest.dto.StockMovementResponse;
import software.openlab.stock.infra.rest.dto.StockResponse;
import software.openlab.stock.infra.rest.dto.UpdateStockRequest;

@Path("/v1/stocks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StockResource {

    @Inject
    CreateStockUseCase createStock;

    @Inject
    ListStocksUseCase listStocks;

    @Inject
    GetStockByIdUseCase getStockById;

    @Inject
    UpdateStockUseCase updateStock;

    @Inject
    DeleteStockUseCase deleteStock;

    @Inject
    ListStockItemsUseCase listStockItems;

    @Inject
    GetStockItemUseCase getStockItem;

    @Inject
    CreateStockMovementUseCase createStockMovement;

    @Inject
    ListStockMovementsUseCase listStockMovements;

    @Inject
    ReserveStockUseCase reserveStock;

    @Inject
    ReleaseStockUseCase releaseStock;

    @Inject
    CreateStockCountUseCase createStockCount;

    @Inject
    ListStockCountsUseCase listStockCounts;

    @GET
    public ListingPageResponse<StockResponse> list(@QueryParam("page") @DefaultValue("1") int page,
                                                     @QueryParam("page_size") @DefaultValue("20") int pageSize,
                                                     @QueryParam("q") String q) {
        PageResult<Stock> result = listStocks.execute(q, page, pageSize);
        return new ListingPageResponse<>(
                result.data().stream().map(StockResponse::from).toList(),
                result.page(), result.pageSize(), result.total());
    }

    @GET
    @Path("/{id}")
    public StockResponse getById(@PathParam("id") String id) {
        return StockResponse.from(getStockById.execute(StockId.of(id)));
    }

    @POST
    public Response create(@Valid CreateStockRequest request) {
        StockResponse response = StockResponse.from(createStock.execute(request.description()));
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @PUT
    @Path("/{id}")
    public StockResponse update(@PathParam("id") String id, @Valid UpdateStockRequest request) {
        return StockResponse.from(updateStock.execute(StockId.of(id), request.description()));
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") String id) {
        deleteStock.execute(StockId.of(id));
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/items")
    public ListingPageResponse<StockItemResponse> listItems(@PathParam("id") String id,
                                                              @QueryParam("page") @DefaultValue("1") int page,
                                                              @QueryParam("page_size") @DefaultValue("20") int pageSize,
                                                              @QueryParam("product_id") String productId,
                                                              @QueryParam("below_min") Boolean belowMin,
                                                              @QueryParam("above_max") Boolean aboveMax) {
        PageResult<StockItem> result = listStockItems.execute(StockId.of(id),
                productId == null ? null : ProductId.of(productId), belowMin, aboveMax, page, pageSize);
        return new ListingPageResponse<>(
                result.data().stream().map(StockItemResponse::from).toList(),
                result.page(), result.pageSize(), result.total());
    }

    @GET
    @Path("/{id}/items/{productId}")
    public StockItemResponse getItem(@PathParam("id") String id, @PathParam("productId") String productId) {
        return StockItemResponse.from(getStockItem.execute(StockId.of(id), ProductId.of(productId)));
    }

    @POST
    @Path("/{id}/movements")
    public Response createMovement(@PathParam("id") String id, @Valid CreateStockMovementRequest request) {
        StockMovement movement = createStockMovement.execute(StockId.of(id), ProductId.of(request.productId()),
                request.type(), request.quantity(), request.reasonId(), request.reference());
        return Response.status(Response.Status.CREATED).entity(StockMovementResponse.from(movement)).build();
    }

    @GET
    @Path("/{id}/movements")
    public ListingPageResponse<StockMovementResponse> listMovements(@PathParam("id") String id,
                                                                      @QueryParam("page") @DefaultValue("1") int page,
                                                                      @QueryParam("page_size") @DefaultValue("20") int pageSize,
                                                                      @QueryParam("product_id") String productId,
                                                                      @QueryParam("type") String type,
                                                                      @QueryParam("from") String from,
                                                                      @QueryParam("to") String to) {
        PageResult<StockMovement> result = listStockMovements.execute(StockId.of(id),
                productId == null ? null : ProductId.of(productId),
                parseMovementType(type), parseInstant(from, "from"), parseInstant(to, "to"), page, pageSize);
        return new ListingPageResponse<>(
                result.data().stream().map(StockMovementResponse::from).toList(),
                result.page(), result.pageSize(), result.total());
    }

    @POST
    @Path("/{id}/items/{productId}/reserve")
    public StockItemResponse reserve(@PathParam("id") String id, @PathParam("productId") String productId,
                                      @Valid QuantityRequest request) {
        return StockItemResponse.from(
                reserveStock.execute(StockId.of(id), ProductId.of(productId), request.quantity()));
    }

    @POST
    @Path("/{id}/items/{productId}/release")
    public StockItemResponse release(@PathParam("id") String id, @PathParam("productId") String productId,
                                      @Valid QuantityRequest request) {
        return StockItemResponse.from(
                releaseStock.execute(StockId.of(id), ProductId.of(productId), request.quantity()));
    }

    @POST
    @Path("/{id}/counts")
    public Response createCount(@PathParam("id") String id, @Valid CreateStockCountRequest request) {
        StockCount count = createStockCount.execute(StockId.of(id), ProductId.of(request.productId()),
                request.countedValue());
        return Response.status(Response.Status.CREATED).entity(StockCountResponse.from(count)).build();
    }

    @GET
    @Path("/{id}/counts")
    public ListingPageResponse<StockCountResponse> listCounts(@PathParam("id") String id,
                                                                @QueryParam("page") @DefaultValue("1") int page,
                                                                @QueryParam("page_size") @DefaultValue("20") int pageSize,
                                                                @QueryParam("product_id") String productId) {
        PageResult<StockCount> result = listStockCounts.execute(StockId.of(id),
                productId == null ? null : ProductId.of(productId), page, pageSize);
        return new ListingPageResponse<>(
                result.data().stream().map(StockCountResponse::from).toList(),
                result.page(), result.pageSize(), result.total());
    }

    private static StockMovementType parseMovementType(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            return StockMovementType.valueOf(raw);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("error.field.invalidType", "type");
        }
    }

    private static Instant parseInstant(String raw, String fieldName) {
        if (raw == null) {
            return null;
        }
        try {
            return Instant.parse(raw);
        } catch (DateTimeParseException e) {
            throw new BadRequestException("error.field.isoDateTime", fieldName);
        }
    }
}
