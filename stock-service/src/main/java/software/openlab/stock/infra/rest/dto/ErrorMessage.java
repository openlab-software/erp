package software.openlab.stock.infra.rest.dto;

/** Generic business-error body per Requirement 13.2: {"message": "..."}. */
public record ErrorMessage(String message) {
}
