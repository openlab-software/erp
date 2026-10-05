package software.openlab.stock.infra.rest.exception;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import software.openlab.stock.infra.i18n.MessageResolver;
import software.openlab.stock.domain.shared.ConflictException;
import software.openlab.stock.infra.rest.dto.ErrorMessage;

@Provider
public class ConflictExceptionMapper implements ExceptionMapper<ConflictException> {

    @Inject
    MessageResolver messages;

    @Override
    public Response toResponse(ConflictException exception) {
        return Response.status(Response.Status.CONFLICT)
                .entity(new ErrorMessage(messages.resolve(exception)))
                .build();
    }
}
