package software.openlab.customer.infra.rest;

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
import software.openlab.customer.application.usecase.contact.AddContactUseCase;
import software.openlab.customer.application.usecase.contact.DeleteContactUseCase;
import software.openlab.customer.application.usecase.contact.ListCustomerContactsUseCase;
import software.openlab.customer.application.usecase.contact.UpdateContactUseCase;
import software.openlab.customer.domain.contact.ContactId;
import software.openlab.customer.domain.customer.CustomerId;
import software.openlab.customer.infra.rest.dto.ContactDTOs.ContactListResponse;
import software.openlab.customer.infra.rest.dto.ContactDTOs.ContactRequest;
import software.openlab.customer.infra.rest.dto.ContactDTOs.ContactResponse;

/** Contact persons of a customer: nested under {@code /v1/customers/{customerId}}. */
@Path("/v1/customers/{customerId}/contacts")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ContactResource {

    @Inject
    ListCustomerContactsUseCase listContacts;

    @Inject
    AddContactUseCase addContact;

    @Inject
    UpdateContactUseCase updateContact;

    @Inject
    DeleteContactUseCase deleteContact;

    @GET
    public ContactListResponse list(@PathParam("customerId") String customerId) {
        return ContactListResponse.from(listContacts.execute(CustomerId.of(customerId)));
    }

    @POST
    public Response create(@PathParam("customerId") String customerId, @Valid ContactRequest request) {
        var added = addContact.execute(CustomerId.of(customerId), request.toDomain());
        return Response.status(Response.Status.CREATED).entity(ContactResponse.from(added)).build();
    }

    @PUT
    @Path("/{contactId}")
    public ContactResponse update(@PathParam("customerId") String customerId,
                                  @PathParam("contactId") String contactId, @Valid ContactRequest request) {
        return ContactResponse.from(updateContact.execute(CustomerId.of(customerId), ContactId.of(contactId),
                request.toDomain()));
    }

    @DELETE
    @Path("/{contactId}")
    public Response delete(@PathParam("customerId") String customerId, @PathParam("contactId") String contactId) {
        deleteContact.execute(CustomerId.of(customerId), ContactId.of(contactId));
        return Response.noContent().build();
    }
}
