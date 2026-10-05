package software.openlab.stock.infra.rest.dto;

import java.util.Map;

/** Payload-validation-error body per Requirement 13.4: {"mensagem": "...", "erros": {field: reason}}. */
public record ValidationErrorBody(String mensagem, Map<String, String> erros) {
}
