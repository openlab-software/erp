package software.openlab.sales.infra.rest;

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
import software.openlab.sales.application.usecase.order.CancelSalesOrderUseCase;
import software.openlab.sales.application.usecase.order.ConfirmSalesOrderUseCase;
import software.openlab.sales.application.usecase.order.CreateSalesOrderUseCase;
import software.openlab.sales.application.usecase.order.DeleteSalesOrderUseCase;
import software.openlab.sales.application.usecase.order.GetSalesOrderByIdUseCase;
import software.openlab.sales.application.usecase.order.GetSalesSummaryUseCase;
import software.openlab.sales.application.usecase.order.ListSalesOrdersUseCase;
import software.openlab.sales.application.usecase.order.LockSalesOrderUseCase;
import software.openlab.sales.application.usecase.order.ResetSalesOrderToDraftUseCase;
import software.openlab.sales.application.usecase.order.SendSalesOrderUseCase;
import software.openlab.sales.application.usecase.order.UpdateSalesOrderUseCase;
import software.openlab.sales.domain.order.SalesOrderId;
import software.openlab.sales.infra.rest.dto.SalesOrderDTOs.SalesOrderPageResponse;
import software.openlab.sales.infra.rest.dto.SalesOrderDTOs.SalesOrderRequest;
import software.openlab.sales.infra.rest.dto.SalesOrderDTOs.SalesOrderResponse;
import software.openlab.sales.infra.rest.dto.SalesOrderDTOs.SalesSummaryResponse;

@Path("/v1/sales-orders")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SalesOrderResource {

    @Inject
    CreateSalesOrderUseCase createOrder;

    @Inject
    GetSalesOrderByIdUseCase getOrderById;

    @Inject
    ListSalesOrdersUseCase listOrders;

    @Inject
    UpdateSalesOrderUseCase updateOrder;

    @Inject
    DeleteSalesOrderUseCase deleteOrder;

    @Inject
    SendSalesOrderUseCase sendOrder;

    @Inject
    ConfirmSalesOrderUseCase confirmOrder;

    @Inject
    CancelSalesOrderUseCase cancelOrder;

    @Inject
    ResetSalesOrderToDraftUseCase resetOrder;

    @Inject
    LockSalesOrderUseCase lockOrder;

    @Inject
    GetSalesSummaryUseCase getSummary;

    @POST
    public Response create(@Valid SalesOrderRequest request) {
        var created = createOrder.execute(request.toCommand());
        return Response.status(Response.Status.CREATED).entity(SalesOrderResponse.from(created)).build();
    }

    @GET
    public SalesOrderPageResponse list(@QueryParam("q") String q,
                                       @QueryParam("state") String state,
                                       @QueryParam("customer_id") String customerId,
                                       @QueryParam("delivery_status") String deliveryStatus,
                                       @QueryParam("invoice_status") String invoiceStatus,
                                       @QueryParam("page") Integer page,
                                       @QueryParam("page_size") Integer pageSize) {
        return SalesOrderPageResponse.from(listOrders.execute(q, state, customerId, deliveryStatus, invoiceStatus,
                page, pageSize));
    }

    @GET
    @Path("/summary")
    public SalesSummaryResponse summary() {
        return SalesSummaryResponse.from(getSummary.execute());
    }

    @GET
    @Path("/{id}")
    public SalesOrderResponse getById(@PathParam("id") String id) {
        return SalesOrderResponse.from(getOrderById.execute(SalesOrderId.of(id)));
    }

    @PUT
    @Path("/{id}")
    public SalesOrderResponse update(@PathParam("id") String id, @Valid SalesOrderRequest request) {
        return SalesOrderResponse.from(updateOrder.execute(SalesOrderId.of(id), request.toCommand()));
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") String id) {
        deleteOrder.execute(SalesOrderId.of(id));
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/send")
    public SalesOrderResponse send(@PathParam("id") String id) {
        return SalesOrderResponse.from(sendOrder.execute(SalesOrderId.of(id)));
    }

    @POST
    @Path("/{id}/confirm")
    public SalesOrderResponse confirm(@PathParam("id") String id) {
        return SalesOrderResponse.from(confirmOrder.execute(SalesOrderId.of(id)));
    }

    @POST
    @Path("/{id}/cancel")
    public SalesOrderResponse cancel(@PathParam("id") String id) {
        return SalesOrderResponse.from(cancelOrder.execute(SalesOrderId.of(id)));
    }

    @POST
    @Path("/{id}/draft")
    public SalesOrderResponse resetToDraft(@PathParam("id") String id) {
        return SalesOrderResponse.from(resetOrder.execute(SalesOrderId.of(id)));
    }

    @POST
    @Path("/{id}/lock")
    public SalesOrderResponse lock(@PathParam("id") String id) {
        return SalesOrderResponse.from(lockOrder.execute(SalesOrderId.of(id)));
    }
}
