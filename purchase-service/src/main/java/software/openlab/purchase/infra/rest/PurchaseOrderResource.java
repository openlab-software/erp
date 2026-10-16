package software.openlab.purchase.infra.rest;

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
import software.openlab.purchase.application.usecase.order.CancelPurchaseOrderUseCase;
import software.openlab.purchase.application.usecase.order.ConfirmPurchaseOrderUseCase;
import software.openlab.purchase.application.usecase.order.CreatePurchaseOrderUseCase;
import software.openlab.purchase.application.usecase.order.DeletePurchaseOrderUseCase;
import software.openlab.purchase.application.usecase.order.GetPurchaseOrderByIdUseCase;
import software.openlab.purchase.application.usecase.order.GetPurchaseSummaryUseCase;
import software.openlab.purchase.application.usecase.order.ListPurchaseOrdersUseCase;
import software.openlab.purchase.application.usecase.order.LockPurchaseOrderUseCase;
import software.openlab.purchase.application.usecase.order.ResetPurchaseOrderToDraftUseCase;
import software.openlab.purchase.application.usecase.order.SendPurchaseOrderUseCase;
import software.openlab.purchase.application.usecase.order.UpdatePurchaseOrderUseCase;
import software.openlab.purchase.domain.order.PurchaseOrderId;
import software.openlab.purchase.infra.rest.dto.PurchaseOrderDTOs.PurchaseOrderPageResponse;
import software.openlab.purchase.infra.rest.dto.PurchaseOrderDTOs.PurchaseOrderRequest;
import software.openlab.purchase.infra.rest.dto.PurchaseOrderDTOs.PurchaseOrderResponse;
import software.openlab.purchase.infra.rest.dto.PurchaseOrderDTOs.PurchaseSummaryResponse;

@Path("/v1/purchase-orders")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PurchaseOrderResource {

    @Inject
    CreatePurchaseOrderUseCase createOrder;

    @Inject
    GetPurchaseOrderByIdUseCase getOrderById;

    @Inject
    ListPurchaseOrdersUseCase listOrders;

    @Inject
    UpdatePurchaseOrderUseCase updateOrder;

    @Inject
    DeletePurchaseOrderUseCase deleteOrder;

    @Inject
    SendPurchaseOrderUseCase sendOrder;

    @Inject
    ConfirmPurchaseOrderUseCase confirmOrder;

    @Inject
    CancelPurchaseOrderUseCase cancelOrder;

    @Inject
    ResetPurchaseOrderToDraftUseCase resetOrder;

    @Inject
    LockPurchaseOrderUseCase lockOrder;

    @Inject
    GetPurchaseSummaryUseCase getSummary;

    @POST
    public Response create(@Valid PurchaseOrderRequest request) {
        var created = createOrder.execute(request.toCommand());
        return Response.status(Response.Status.CREATED).entity(PurchaseOrderResponse.from(created)).build();
    }

    @GET
    public PurchaseOrderPageResponse list(@QueryParam("q") String q,
                                       @QueryParam("state") String state,
                                       @QueryParam("supplier_id") String supplierId,
                                       @QueryParam("receipt_status") String receiptStatus,
                                       @QueryParam("bill_status") String billStatus,
                                       @QueryParam("page") Integer page,
                                       @QueryParam("page_size") Integer pageSize) {
        return PurchaseOrderPageResponse.from(listOrders.execute(q, state, supplierId, receiptStatus, billStatus,
                page, pageSize));
    }

    @GET
    @Path("/summary")
    public PurchaseSummaryResponse summary() {
        return PurchaseSummaryResponse.from(getSummary.execute());
    }

    @GET
    @Path("/{id}")
    public PurchaseOrderResponse getById(@PathParam("id") String id) {
        return PurchaseOrderResponse.from(getOrderById.execute(PurchaseOrderId.of(id)));
    }

    @PUT
    @Path("/{id}")
    public PurchaseOrderResponse update(@PathParam("id") String id, @Valid PurchaseOrderRequest request) {
        return PurchaseOrderResponse.from(updateOrder.execute(PurchaseOrderId.of(id), request.toCommand()));
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") String id) {
        deleteOrder.execute(PurchaseOrderId.of(id));
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/send")
    public PurchaseOrderResponse send(@PathParam("id") String id) {
        return PurchaseOrderResponse.from(sendOrder.execute(PurchaseOrderId.of(id)));
    }

    @POST
    @Path("/{id}/confirm")
    public PurchaseOrderResponse confirm(@PathParam("id") String id) {
        return PurchaseOrderResponse.from(confirmOrder.execute(PurchaseOrderId.of(id)));
    }

    @POST
    @Path("/{id}/cancel")
    public PurchaseOrderResponse cancel(@PathParam("id") String id) {
        return PurchaseOrderResponse.from(cancelOrder.execute(PurchaseOrderId.of(id)));
    }

    @POST
    @Path("/{id}/draft")
    public PurchaseOrderResponse resetToDraft(@PathParam("id") String id) {
        return PurchaseOrderResponse.from(resetOrder.execute(PurchaseOrderId.of(id)));
    }

    @POST
    @Path("/{id}/lock")
    public PurchaseOrderResponse lock(@PathParam("id") String id) {
        return PurchaseOrderResponse.from(lockOrder.execute(PurchaseOrderId.of(id)));
    }
}
