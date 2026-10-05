package software.openlab.stock.infra.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateStockRequest(@NotBlank String description) {
}
