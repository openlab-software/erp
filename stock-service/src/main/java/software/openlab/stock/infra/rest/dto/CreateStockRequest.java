package software.openlab.stock.infra.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateStockRequest(@NotBlank String description) {
}
