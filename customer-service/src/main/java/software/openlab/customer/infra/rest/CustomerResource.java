package software.openlab.customer.infra.rest;

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
import software.openlab.customer.application.usecase.customer.ChangeCustomerStatusUseCase;
import software.openlab.customer.application.usecase.customer.CreateCustomerUseCase;
import software.openlab.customer.application.usecase.customer.DeleteCustomerUseCase;
import software.openlab.customer.application.usecase.customer.GetCustomerByIdUseCase;
import software.openlab.customer.application.usecase.customer.ListCustomersUseCase;
import software.openlab.customer.application.usecase.customer.UpdateCustomerUseCase;
import software.openlab.customer.domain.customer.CustomerId;
import software.openlab.customer.infra.rest.dto.CustomerDTOs.ChangeStatusRequest;
import software.openlab.customer.infra.rest.dto.CustomerDTOs.CreateCustomerRequest;
import software.openlab.customer.infra.rest.dto.CustomerDTOs.CustomerPageResponse;
import software.openlab.customer.infra.rest.dto.CustomerDTOs.CustomerResponse;
import software.openlab.customer.infra.rest.dto.CustomerDTOs.UpdateCustomerRequest;

@Path("/v1/customers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CustomerResource {

    @Inject
    CreateCustomerUseCase createCustomer;

    @Inject
    GetCustomerByIdUseCase getCustomerById;

    @Inject
    ListCustomersUseCase listCustomers;

    @Inject
    UpdateCustomerUseCase updateCustomer;

    @Inject
    ChangeCustomerStatusUseCase changeCustomerStatus;

    @Inject
    DeleteCustomerUseCase deleteCustomer;

    @POST
    public Response create(@Valid CreateCustomerRequest request) {
        var created = createCustomer.execute(request.type(), request.toCommand());
        return Response.status(Response.Status.CREATED).entity(CustomerResponse.from(created)).build();
    }

    @GET
    public CustomerPageResponse list(@QueryParam("q") String q,
                                     @QueryParam("type") String type,
                                     @QueryParam("status") String status,
                                     @QueryParam("page") Integer page,
                                     @QueryParam("page_size") Integer pageSize) {
        return CustomerPageResponse.from(listCustomers.execute(q, type, status, page, pageSize));
    }

    @GET
    @Path("/{id}")
    public CustomerResponse getById(@PathParam("id") String id) {
        return CustomerResponse.from(getCustomerById.execute(CustomerId.of(id)));
    }

    @PUT
    @Path("/{id}")
    public CustomerResponse update(@PathParam("id") String id, @Valid UpdateCustomerRequest request) {
        return CustomerResponse.from(updateCustomer.execute(CustomerId.of(id), request.toCommand()));
    }

    @PATCH
    @Path("/{id}/status")
    public CustomerResponse changeStatus(@PathParam("id") String id, @Valid ChangeStatusRequest request) {
        return CustomerResponse.from(changeCustomerStatus.execute(CustomerId.of(id), request.status()));
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") String id) {
        // Its addresses are removed with it (ON DELETE CASCADE on customer_addresses).
        deleteCustomer.execute(CustomerId.of(id));
        return Response.noContent().build();
    }
}
