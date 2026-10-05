package software.openlab.stock.infra.rest;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import software.openlab.stock.application.usecase.reassignment.CreateReassignmentUseCase;
import software.openlab.stock.application.usecase.reassignment.CreateReassignmentUseCase.ReassignmentItemRequest;
import software.openlab.stock.domain.shared.ProductId;
import software.openlab.stock.domain.stock.StockId;
import software.openlab.stock.infra.rest.dto.CreateReassignmentRequest;
import software.openlab.stock.infra.rest.dto.ReassignmentResponse;

@Path("/v1/reassignments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReassignmentResource {

    @Inject
    CreateReassignmentUseCase createReassignment;

    @POST
    public Response create(@Valid CreateReassignmentRequest request) {
        List<ReassignmentItemRequest> items = request.items().stream()
                .map(i -> new ReassignmentItemRequest(ProductId.of(i.productId()), i.quantity()))
                .toList();

        ReassignmentResponse response = ReassignmentResponse.from(
                createReassignment.execute(
                        StockId.of(request.fromStockId()), StockId.of(request.toStockId()), items));
        return Response.status(Response.Status.CREATED).entity(response).build();
    }
}
