package software.openlab.accounting.infra.rest;

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
import software.openlab.accounting.application.usecase.invoice.CancelInvoiceUseCase;
import software.openlab.accounting.application.usecase.invoice.CreateInvoiceUseCase;
import software.openlab.accounting.application.usecase.invoice.DeleteInvoiceUseCase;
import software.openlab.accounting.application.usecase.invoice.GetAccountingPositionUseCase;
import software.openlab.accounting.application.usecase.invoice.GetInvoiceByIdUseCase;
import software.openlab.accounting.application.usecase.invoice.ListInvoicesUseCase;
import software.openlab.accounting.application.usecase.invoice.PostInvoiceUseCase;
import software.openlab.accounting.application.usecase.invoice.RegisterPaymentUseCase;
import software.openlab.accounting.application.usecase.invoice.ResetInvoiceToDraftUseCase;
import software.openlab.accounting.application.usecase.invoice.UpdateInvoiceUseCase;
import software.openlab.accounting.domain.invoice.InvoiceId;
import software.openlab.accounting.infra.rest.dto.InvoiceDTOs.AccountingPositionResponse;
import software.openlab.accounting.infra.rest.dto.InvoiceDTOs.InvoicePageResponse;
import software.openlab.accounting.infra.rest.dto.InvoiceDTOs.InvoiceRequest;
import software.openlab.accounting.infra.rest.dto.InvoiceDTOs.InvoiceResponse;
import software.openlab.accounting.infra.rest.dto.InvoiceDTOs.PaymentRequest;

@Path("/v1")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class InvoiceResource {

    @Inject
    CreateInvoiceUseCase createInvoice;

    @Inject
    GetInvoiceByIdUseCase getInvoiceById;

    @Inject
    ListInvoicesUseCase listInvoices;

    @Inject
    UpdateInvoiceUseCase updateInvoice;

    @Inject
    DeleteInvoiceUseCase deleteInvoice;

    @Inject
    PostInvoiceUseCase postInvoice;

    @Inject
    CancelInvoiceUseCase cancelInvoice;

    @Inject
    ResetInvoiceToDraftUseCase resetInvoice;

    @Inject
    RegisterPaymentUseCase registerPayment;

    @Inject
    GetAccountingPositionUseCase getPosition;

    @POST
    @Path("/invoices")
    public Response create(@Valid InvoiceRequest request) {
        var created = createInvoice.execute(request.kind() == null ? "CUSTOMER_INVOICE" : request.kind(),
                request.toCommand());
        return Response.status(Response.Status.CREATED).entity(InvoiceResponse.from(created)).build();
    }

    @GET
    @Path("/invoices")
    public InvoicePageResponse list(@QueryParam("q") String q,
                                    @QueryParam("kind") String kind,
                                    @QueryParam("state") String state,
                                    @QueryParam("payment_state") String paymentState,
                                    @QueryParam("partner_id") String partnerId,
                                    @QueryParam("overdue") Boolean overdue,
                                    @QueryParam("page") Integer page,
                                    @QueryParam("page_size") Integer pageSize) {
        return InvoicePageResponse.from(listInvoices.execute(q, kind, state, paymentState, partnerId, overdue, page,
                pageSize));
    }

    @GET
    @Path("/invoices/{id}")
    public InvoiceResponse getById(@PathParam("id") String id) {
        return InvoiceResponse.from(getInvoiceById.execute(InvoiceId.of(id)));
    }

    @PUT
    @Path("/invoices/{id}")
    public InvoiceResponse update(@PathParam("id") String id, @Valid InvoiceRequest request) {
        return InvoiceResponse.from(updateInvoice.execute(InvoiceId.of(id), request.toCommand()));
    }

    @DELETE
    @Path("/invoices/{id}")
    public Response delete(@PathParam("id") String id) {
        deleteInvoice.execute(InvoiceId.of(id));
        return Response.noContent().build();
    }

    @POST
    @Path("/invoices/{id}/post")
    public InvoiceResponse post(@PathParam("id") String id) {
        return InvoiceResponse.from(postInvoice.execute(InvoiceId.of(id)));
    }

    @POST
    @Path("/invoices/{id}/cancel")
    public InvoiceResponse cancel(@PathParam("id") String id) {
        return InvoiceResponse.from(cancelInvoice.execute(InvoiceId.of(id)));
    }

    @POST
    @Path("/invoices/{id}/draft")
    public InvoiceResponse resetToDraft(@PathParam("id") String id) {
        return InvoiceResponse.from(resetInvoice.execute(InvoiceId.of(id)));
    }

    @POST
    @Path("/invoices/{id}/payments")
    public Response registerPayment(@PathParam("id") String id, @Valid PaymentRequest request) {
        var invoice = registerPayment.execute(InvoiceId.of(id), request.amount(), request.paidOn(), request.method(),
                request.reference());
        return Response.status(Response.Status.CREATED).entity(InvoiceResponse.from(invoice)).build();
    }

    @GET
    @Path("/accounting/position")
    public AccountingPositionResponse position() {
        return AccountingPositionResponse.from(getPosition.execute());
    }
}
