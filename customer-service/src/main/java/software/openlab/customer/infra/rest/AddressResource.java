package software.openlab.customer.infra.rest;

import jakarta.inject.Inject;
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
import software.openlab.customer.application.usecase.address.AddAddressUseCase;
import software.openlab.customer.application.usecase.address.DeleteAddressUseCase;
import software.openlab.customer.application.usecase.address.ListCustomerAddressesUseCase;
import software.openlab.customer.application.usecase.address.UpdateAddressUseCase;
import software.openlab.customer.domain.address.AddressId;
import software.openlab.customer.domain.customer.CustomerId;
import software.openlab.customer.infra.rest.dto.AddressDTOs.AddressListResponse;
import software.openlab.customer.infra.rest.dto.AddressDTOs.AddressRequest;
import software.openlab.customer.infra.rest.dto.AddressDTOs.AddressResponse;

/** Addresses of a customer: a module of its own, nested under {@code /v1/customers/{customerId}}. */
@Path("/v1/customers/{customerId}/addresses")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AddressResource {

    @Inject
    ListCustomerAddressesUseCase listAddresses;

    @Inject
    AddAddressUseCase addAddress;

    @Inject
    UpdateAddressUseCase updateAddress;

    @Inject
    DeleteAddressUseCase deleteAddress;

    @GET
    public AddressListResponse list(@PathParam("customerId") String customerId) {
        return AddressListResponse.from(listAddresses.execute(CustomerId.of(customerId)));
    }

    @POST
    public Response create(@PathParam("customerId") String customerId, AddressRequest request) {
        var added = addAddress.execute(CustomerId.of(customerId), request.toDomain());
        return Response.status(Response.Status.CREATED).entity(AddressResponse.from(added)).build();
    }

    @PUT
    @Path("/{addressId}")
    public AddressResponse update(@PathParam("customerId") String customerId,
                                  @PathParam("addressId") String addressId, AddressRequest request) {
        return AddressResponse.from(updateAddress.execute(CustomerId.of(customerId), AddressId.of(addressId),
                request.toDomain()));
    }

    @DELETE
    @Path("/{addressId}")
    public Response delete(@PathParam("customerId") String customerId, @PathParam("addressId") String addressId) {
        deleteAddress.execute(CustomerId.of(customerId), AddressId.of(addressId));
        return Response.noContent().build();
    }
}
