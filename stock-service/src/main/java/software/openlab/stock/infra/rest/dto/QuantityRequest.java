package software.openlab.stock.infra.rest.dto;

/** Shared by reserve/release (Requirement 6) — {@code quantity} positivity is a business rule
 * validated in the use case, not bean validation, so the response error shape stays {"message"}. */
public record QuantityRequest(int quantity) {
}
