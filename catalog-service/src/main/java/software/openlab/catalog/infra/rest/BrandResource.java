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
import software.openlab.catalog.application.usecase.brand.CreateBrandUseCase;
import software.openlab.catalog.application.usecase.brand.DeleteBrandUseCase;
import software.openlab.catalog.application.usecase.brand.ListBrandsUseCase;
import software.openlab.catalog.application.usecase.brand.UpdateBrandUseCase;
import software.openlab.catalog.domain.brand.BrandId;
import software.openlab.catalog.infra.rest.dto.BrandDTOs.BrandPageResponse;
import software.openlab.catalog.infra.rest.dto.BrandDTOs.BrandResponse;
import software.openlab.catalog.infra.rest.dto.BrandDTOs.CreateBrandRequest;
import software.openlab.catalog.infra.rest.dto.BrandDTOs.UpdateBrandRequest;

@Path("/v1/brands")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class BrandResource {

    @Inject
    CreateBrandUseCase createBrand;

    @Inject
    ListBrandsUseCase listBrands;

    @Inject
    UpdateBrandUseCase updateBrand;

    @Inject
    DeleteBrandUseCase deleteBrand;

    @POST
    public Response create(@Valid CreateBrandRequest request) {
        var created = createBrand.execute(request.description());
        return Response.status(Response.Status.CREATED).entity(BrandResponse.from(created)).build();
    }

    @GET
    public BrandPageResponse list(@QueryParam("q") String q,
                                   @QueryParam("page") Integer page,
                                   @QueryParam("page_size") Integer pageSize) {
        return BrandPageResponse.from(listBrands.execute(q, page, pageSize));
    }

    @PUT
    @Path("/{id}")
    public BrandResponse update(@PathParam("id") String id, @Valid UpdateBrandRequest request) {
        return BrandResponse.from(updateBrand.execute(BrandId.of(id), request.description()));
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") String id) {
        deleteBrand.execute(BrandId.of(id));
        return Response.noContent().build();
    }
}
