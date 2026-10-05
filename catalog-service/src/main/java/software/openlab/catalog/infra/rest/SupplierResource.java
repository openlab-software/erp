package software.openlab.catalog.infra.rest;

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
import software.openlab.catalog.application.usecase.supplier.CreateSupplierUseCase;
import software.openlab.catalog.application.usecase.supplier.DeleteSupplierUseCase;
import software.openlab.catalog.application.usecase.supplier.ListSuppliersUseCase;
import software.openlab.catalog.application.usecase.supplier.UpdateSupplierUseCase;
import software.openlab.catalog.domain.supplier.SupplierId;
import software.openlab.catalog.infra.rest.dto.SupplierDTOs.CreateSupplierRequest;
import software.openlab.catalog.infra.rest.dto.SupplierDTOs.SupplierPageResponse;
import software.openlab.catalog.infra.rest.dto.SupplierDTOs.SupplierResponse;
import software.openlab.catalog.infra.rest.dto.SupplierDTOs.UpdateSupplierRequest;

@Path("/v1/suppliers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SupplierResource {

    @Inject
    CreateSupplierUseCase createSupplier;

    @Inject
    ListSuppliersUseCase listSuppliers;

    @Inject
    UpdateSupplierUseCase updateSupplier;

    @Inject
    DeleteSupplierUseCase deleteSupplier;

    @POST
    public Response create(@Valid CreateSupplierRequest request) {
        var created = createSupplier.execute(request.name(), request.document());
        return Response.status(Response.Status.CREATED).entity(SupplierResponse.from(created)).build();
    }

    @GET
    public SupplierPageResponse list(@QueryParam("q") String q,
                                      @QueryParam("page") Integer page,
                                      @QueryParam("page_size") Integer pageSize) {
        return SupplierPageResponse.from(listSuppliers.execute(q, page, pageSize));
    }

    @PUT
    @Path("/{id}")
    public SupplierResponse update(@PathParam("id") String id, @Valid UpdateSupplierRequest request) {
        return SupplierResponse.from(updateSupplier.execute(SupplierId.of(id), request.name(), request.document()));
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") String id) {
        deleteSupplier.execute(SupplierId.of(id));
        return Response.noContent().build();
    }
}
