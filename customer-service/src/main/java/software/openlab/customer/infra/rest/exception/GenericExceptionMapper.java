package software.openlab.customer.infra.rest.exception;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import software.openlab.customer.infra.i18n.MessageResolver;
import java.util.Map;
import org.jboss.logging.Logger;

/** Catch-all safety net so unexpected failures never leak stack traces to clients. */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    @Inject
    MessageResolver messages;

    private static final Logger LOG = Logger.getLogger(GenericExceptionMapper.class);

    @Override
    public Response toResponse(Throwable exception) {
        LOG.error("unhandled exception", exception);
        return Response.status(500)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("message", messages.resolve("error.internal")))
                .build();
    }
}
