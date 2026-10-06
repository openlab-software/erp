package software.openlab.stock.infra.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * REST client for catalog-service. The base URL ({@code stork://catalog-service/...}) is
 * resolved by Stork, which load-balances across the Kubernetes endpoints of the service.
 */
@RegisterRestClient(configKey = "catalog")
@Path("/v1/products")
public interface CatalogClient {

    @GET
    @Path("/{id}")
    ProductView getProduct(@PathParam("id") String id);

    // catalog-service serializes camelCase, while this service defaults to SNAKE_CASE.
    @JsonIgnoreProperties(ignoreUnknown = true)
    record ProductView(
            @JsonProperty("productId") String productId,
            @JsonProperty("description") String description,
            @JsonProperty("status") String status) {
    }
}
