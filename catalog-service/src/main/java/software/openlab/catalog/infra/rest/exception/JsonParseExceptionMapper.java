package software.openlab.catalog.infra.rest.exception;

import jakarta.inject.Inject;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import software.openlab.catalog.infra.i18n.MessageResolver;
import java.util.Map;

/** Malformed request bodies map to the same standardized 400 shape (Requirement 13.1). */
@Provider
public class JsonParseExceptionMapper implements ExceptionMapper<JsonProcessingException> {

    @Inject
    MessageResolver messages;

    @Override
    public Response toResponse(JsonProcessingException exception) {
        return Response.status(400)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("message", messages.resolve("error.json.malformed")))
                .build();
    }
}
