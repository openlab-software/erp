package software.openlab.stock.infra.rest.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateReassignmentItemRequest(@NotBlank String productId, @Min(1) int quantity) {
}
