package software.openlab.stock.infra.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateStockMovementRequest(@NotBlank String productId, @NotBlank String type, int quantity,
                                          String reasonId, String reference) {
}
