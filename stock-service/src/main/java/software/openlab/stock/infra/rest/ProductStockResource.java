package software.openlab.stock.infra.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import software.openlab.stock.application.usecase.stockitem.GetProductStockBalanceUseCase;
import software.openlab.stock.domain.shared.ProductId;
import software.openlab.stock.infra.rest.dto.ProductStockBalanceResponse;

@Path("/v1/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductStockResource {

    @Inject
    GetProductStockBalanceUseCase getProductStockBalance;

    @GET
    @Path("/{productId}/balance")
    public ProductStockBalanceResponse getBalance(@PathParam("productId") String productId) {
        return ProductStockBalanceResponse.from(getProductStockBalance.execute(ProductId.of(productId)));
    }
}
