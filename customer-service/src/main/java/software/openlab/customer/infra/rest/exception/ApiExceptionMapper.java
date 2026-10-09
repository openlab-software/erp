package software.openlab.customer.infra.rest.exception;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import software.openlab.customer.infra.i18n.MessageResolver;
import java.util.Map;
import software.openlab.customer.domain.shared.ApiException;

/**
 * Standardized error body for business/domain errors (Requirement 13.1):
 * {@code {"message": "<descrição>"}}.
 */
@Provider
public class ApiExceptionMapper implements ExceptionMapper<ApiException> {

    @Inject
    MessageResolver messages;

    @Override
    public Response toResponse(ApiException exception) {
        return Response.status(exception.getStatus())
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("message", messages.resolve(exception)))
                .build();
    }
}
