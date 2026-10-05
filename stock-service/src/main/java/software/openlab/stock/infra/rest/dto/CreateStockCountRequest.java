package software.openlab.stock.infra.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateStockCountRequest(@NotBlank String productId, int countedValue) {
}
