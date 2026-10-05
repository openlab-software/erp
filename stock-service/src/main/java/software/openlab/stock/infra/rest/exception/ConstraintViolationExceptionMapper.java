package software.openlab.stock.infra.rest.exception;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import software.openlab.stock.infra.i18n.ViolationMessages;
import java.util.LinkedHashMap;
import java.util.Map;
import software.openlab.stock.infra.rest.dto.ValidationErrorBody;

/**
 * Bean-validation failures (@NotBlank, @NotEmpty, @Min, ...) map to Requirement 13.3/13.4's
 * shape: {"mensagem": "Um ou mais campos são inválidos", "erros": {field: reason}}.
 */
@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Inject
    ViolationMessages violationMessages;

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        Map<String, String> erros = new LinkedHashMap<>();
        for (ConstraintViolation<?> violation : exception.getConstraintViolations()) {
            erros.put(toSnakeCase(leafFieldName(violation.getPropertyPath())), violationMessages.messageFor(violation));
        }
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ValidationErrorBody(violationMessages.summary(), erros))
                .build();
    }

    private static String leafFieldName(Path path) {
        String last = null;
        for (Path.Node node : path) {
            if (node.getName() != null) {
                last = node.getName();
            }
        }
        return last != null ? last : "value";
    }

    private static String toSnakeCase(String camelCase) {
        StringBuilder sb = new StringBuilder();
        for (char c : camelCase.toCharArray()) {
            if (Character.isUpperCase(c)) {
                sb.append('_').append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
