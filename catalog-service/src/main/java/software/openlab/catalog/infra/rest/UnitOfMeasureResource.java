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
import software.openlab.catalog.application.usecase.unitofmeasure.CreateUnitOfMeasureUseCase;
import software.openlab.catalog.application.usecase.unitofmeasure.DeleteUnitOfMeasureUseCase;
import software.openlab.catalog.application.usecase.unitofmeasure.ListUnitOfMeasuresUseCase;
import software.openlab.catalog.application.usecase.unitofmeasure.UpdateUnitOfMeasureUseCase;
import software.openlab.catalog.domain.unitofmeasure.UnitOfMeasureId;
import software.openlab.catalog.infra.rest.dto.UnitOfMeasureDTOs.CreateUnitOfMeasureRequest;
import software.openlab.catalog.infra.rest.dto.UnitOfMeasureDTOs.UnitOfMeasurePageResponse;
import software.openlab.catalog.infra.rest.dto.UnitOfMeasureDTOs.UnitOfMeasureResponse;
import software.openlab.catalog.infra.rest.dto.UnitOfMeasureDTOs.UpdateUnitOfMeasureRequest;

@Path("/v1/units-of-measure")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UnitOfMeasureResource {

    @Inject
    CreateUnitOfMeasureUseCase createUnitOfMeasure;

    @Inject
    ListUnitOfMeasuresUseCase listUnitOfMeasures;

    @Inject
    UpdateUnitOfMeasureUseCase updateUnitOfMeasure;

    @Inject
    DeleteUnitOfMeasureUseCase deleteUnitOfMeasure;

    @POST
    public Response create(@Valid CreateUnitOfMeasureRequest request) {
        var created = createUnitOfMeasure.execute(request.code(), request.description());
        return Response.status(Response.Status.CREATED).entity(UnitOfMeasureResponse.from(created)).build();
    }

    @GET
    public UnitOfMeasurePageResponse list(@QueryParam("q") String q,
                                           @QueryParam("page") Integer page,
                                           @QueryParam("page_size") Integer pageSize) {
        return UnitOfMeasurePageResponse.from(listUnitOfMeasures.execute(q, page, pageSize));
    }

    @PUT
    @Path("/{id}")
    public UnitOfMeasureResponse update(@PathParam("id") String id, @Valid UpdateUnitOfMeasureRequest request) {
        return UnitOfMeasureResponse.from(updateUnitOfMeasure.execute(UnitOfMeasureId.of(id), request.code(), request.description()));
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") String id) {
        deleteUnitOfMeasure.execute(UnitOfMeasureId.of(id));
        return Response.noContent().build();
    }
}
