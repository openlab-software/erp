package software.openlab.catalog.infra.rest.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;
import software.openlab.catalog.domain.shared.PageResult;
import software.openlab.catalog.domain.supplier.Supplier;

public final class SupplierDTOs {

    private SupplierDTOs() {
    }

    public record CreateSupplierRequest(@NotBlank String name, @NotBlank String document) {
    }

    public record UpdateSupplierRequest(@NotBlank String name, @NotBlank String document) {
    }

    public record SupplierResponse(String supplierId, String name, String document, Instant createdAt, Instant updatedAt) {
        public static SupplierResponse from(Supplier s) {
            return new SupplierResponse(s.getSupplierId().toString(), s.getName(), s.getDocument(), s.getCreatedAt(), s.getUpdatedAt());
        }
    }

    public record SupplierPageResponse(List<SupplierResponse> data, int page, int pageSize, long total) {
        public static SupplierPageResponse from(PageResult<Supplier> page) {
            List<SupplierResponse> data = page.data().stream().map(SupplierResponse::from).toList();
            return new SupplierPageResponse(data, page.page(), page.pageSize(), page.total());
        }
    }
}
