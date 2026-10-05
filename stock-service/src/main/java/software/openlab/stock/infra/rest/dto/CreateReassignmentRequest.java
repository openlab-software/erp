package software.openlab.stock.infra.rest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CreateReassignmentRequest(
        @NotBlank String fromStockId,
        @NotBlank String toStockId,
        @NotEmpty List<@Valid CreateReassignmentItemRequest> items) {
}
