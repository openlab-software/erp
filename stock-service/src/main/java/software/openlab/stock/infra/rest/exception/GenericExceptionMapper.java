package software.openlab.stock.infra.rest.exception;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import software.openlab.stock.infra.i18n.MessageResolver;
import org.jboss.logging.Logger;
import software.openlab.stock.infra.rest.dto.ErrorMessage;

/** Safety net for anything not covered by a more specific mapper. */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Exception> {

    @Inject
    MessageResolver messages;

    private static final Logger LOG = Logger.getLogger(GenericExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception) {
        LOG.error("unhandled exception while processing request", exception);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorMessage(messages.resolve("error.internal")))
                .build();
    }
}
