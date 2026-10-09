package software.openlab.customer.infra.rest.exception;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import software.openlab.customer.infra.i18n.ViolationMessages;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Standardized payload-validation error (Requirement 13.3):
 * {@code {"mensagem": violationMessages.summary(), "erros": {<campo>: <descrição>}}}.
 */
@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Inject
    ViolationMessages violationMessages;

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        Map<String, String> errors = new LinkedHashMap<>();

        for (ConstraintViolation<?> violation : exception.getConstraintViolations()) {
            errors.put(lastNode(violation.getPropertyPath()), violationMessages.messageFor(violation));
        }

        return Response.status(400)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("mensagem", violationMessages.summary(), "erros", errors))
                .build();
    }

    private static String lastNode(Path path) {
        String last = "campo";
        Iterator<Path.Node> it = path.iterator();
        while (it.hasNext()) {
            last = it.next().getName();
        }
        return last;
    }
}
