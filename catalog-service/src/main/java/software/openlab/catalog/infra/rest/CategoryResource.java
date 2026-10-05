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
import software.openlab.catalog.application.usecase.category.CreateCategoryUseCase;
import software.openlab.catalog.application.usecase.category.DeleteCategoryUseCase;
import software.openlab.catalog.application.usecase.category.GetCategoryByIdUseCase;
import software.openlab.catalog.application.usecase.category.ListCategoriesUseCase;
import software.openlab.catalog.application.usecase.category.UpdateCategoryUseCase;
import software.openlab.catalog.domain.category.CategoryId;
import software.openlab.catalog.infra.rest.dto.CategoryDTOs.CategoryPageResponse;
import software.openlab.catalog.infra.rest.dto.CategoryDTOs.CategoryResponse;
import software.openlab.catalog.infra.rest.dto.CategoryDTOs.CreateCategoryRequest;
import software.openlab.catalog.infra.rest.dto.CategoryDTOs.UpdateCategoryRequest;

@Path("/v1/categories")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CategoryResource {

    @Inject
    CreateCategoryUseCase createCategory;

    @Inject
    GetCategoryByIdUseCase getCategoryById;

    @Inject
    ListCategoriesUseCase listCategories;

    @Inject
    UpdateCategoryUseCase updateCategory;

    @Inject
    DeleteCategoryUseCase deleteCategory;

    @POST
    public Response create(@Valid CreateCategoryRequest request) {
        CategoryId parentCategoryId = toParentCategoryId(request.parentCategoryId());
        var created = createCategory.execute(request.description(), parentCategoryId);
        return Response.status(Response.Status.CREATED).entity(CategoryResponse.from(created)).build();
    }

    @GET
    public CategoryPageResponse list(@QueryParam("q") String q,
                                      @QueryParam("page") Integer page,
                                      @QueryParam("page_size") Integer pageSize) {
        return CategoryPageResponse.from(listCategories.execute(q, page, pageSize));
    }

    @GET
    @Path("/{id}")
    public CategoryResponse getById(@PathParam("id") String id) {
        return CategoryResponse.from(getCategoryById.execute(CategoryId.of(id)));
    }

    @PUT
    @Path("/{id}")
    public CategoryResponse update(@PathParam("id") String id, @Valid UpdateCategoryRequest request) {
        CategoryId parentCategoryId = toParentCategoryId(request.parentCategoryId());
        return CategoryResponse.from(updateCategory.execute(CategoryId.of(id), request.description(), parentCategoryId));
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") String id) {
        deleteCategory.execute(CategoryId.of(id));
        return Response.noContent().build();
    }

    private CategoryId toParentCategoryId(String raw) {
        return (raw == null || raw.isBlank()) ? null : CategoryId.of(raw);
    }
}
