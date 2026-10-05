package software.openlab.catalog.infra.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import software.openlab.catalog.application.usecase.product.ProductImportReport;
import software.openlab.catalog.domain.product.PriceHistory;
import software.openlab.catalog.domain.product.Product;
import software.openlab.catalog.domain.product.ProductAttribute;
import software.openlab.catalog.domain.product.ProductBarcode;
import software.openlab.catalog.domain.product.ProductImage;
import software.openlab.catalog.domain.shared.PageResult;

public final class ProductDTOs {

    private ProductDTOs() {
    }

    public record CreateProductRequest(
            @NotBlank String description,
            @NotBlank String shortDescription,
            @NotBlank String type,
            @NotBlank String unitOfMeasureId,
            @NotBlank String categoryId,
            String brandId,
            String defaultSupplierId
    ) {
    }

    /**
     * {@code type} is accepted here only so a client that resends the whole
     * product body doesn't get an "unknown field" 400 — it is always ignored
     * (Requirement 6.3: type is immutable after creation).
     */
    public record UpdateProductRequest(
            @NotBlank String description,
            @NotBlank String shortDescription,
            String type,
            @NotBlank String unitOfMeasureId,
            @NotBlank String categoryId,
            String brandId,
            String defaultSupplierId
    ) {
    }

    public record ChangeStatusRequest(@NotBlank String status) {
    }

    public record CategoryRefResponse(String categoryId, String description) {
    }

    public record UnitOfMeasureRefResponse(String unitOfMeasureId, String code) {
    }

    public record BrandRefResponse(String brandId, String description) {
    }

    public record AttributeItem(@NotBlank String name, @NotBlank String value) {
    }

    public record ReplaceAttributesRequest(@NotNull @Valid List<AttributeItem> attributes) {
    }

    public record AttributeResponse(String name, String value) {
        public static AttributeResponse from(ProductAttribute a) {
            return new AttributeResponse(a.name(), a.value());
        }
    }

    public record AddBarcodeRequest(@NotBlank String code, @NotBlank String type) {
    }

    public record BarcodeResponse(String barcodeId, String code, String type, Instant createdAt) {
        public static BarcodeResponse from(ProductBarcode b) {
            return new BarcodeResponse(b.getBarcodeId().toString(), b.getCode(), b.getType().name(), b.getCreatedAt());
        }
    }

    public record AddImageRequest(@NotBlank String url) {
    }

    public record ImageResponse(String imageId, String url, boolean isPrimary, Instant createdAt) {
        public static ImageResponse from(ProductImage i) {
            return new ImageResponse(i.getImageId().toString(), i.getUrl(), i.isPrimary(), i.getCreatedAt());
        }
    }

    public record ChangePriceRequest(@NotNull @PositiveOrZero BigDecimal salePrice, @NotNull @PositiveOrZero BigDecimal costPrice) {
    }

    public record PriceHistoryResponse(String priceHistoryId, BigDecimal salePrice, BigDecimal costPrice, Instant changedAt) {
        public static PriceHistoryResponse from(PriceHistory p) {
            return new PriceHistoryResponse(p.priceHistoryId().toString(), p.salePrice(), p.costPrice(), p.changedAt());
        }
    }

    public record ProductResponse(
            String productId,
            String description,
            String shortDescription,
            String type,
            UnitOfMeasureRefResponse unitOfMeasure,
            String status,
            CategoryRefResponse category,
            BrandRefResponse brand,
            String defaultSupplierId,
            BigDecimal salePrice,
            BigDecimal costPrice,
            List<AttributeResponse> attributes,
            List<BarcodeResponse> barcodes,
            List<ImageResponse> images,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static ProductResponse from(Product p, List<ProductAttribute> attributes, List<ProductBarcode> barcodes,
                                            List<ProductImage> images) {
            return new ProductResponse(
                    p.getProductId().toString(),
                    p.getDescription(),
                    p.getShortDescription(),
                    p.getType().name(),
                    new UnitOfMeasureRefResponse(p.getUnitOfMeasureId().toString(), p.getUnitOfMeasureCode()),
                    p.getStatus().name(),
                    new CategoryRefResponse(p.getCategoryId().toString(), p.getCategoryDescription()),
                    p.getBrandId() != null ? new BrandRefResponse(p.getBrandId().toString(), p.getBrandDescription()) : null,
                    p.getDefaultSupplierId() != null ? p.getDefaultSupplierId().toString() : null,
                    p.getSalePrice(),
                    p.getCostPrice(),
                    attributes.stream().map(AttributeResponse::from).toList(),
                    barcodes.stream().map(BarcodeResponse::from).toList(),
                    images.stream().map(ImageResponse::from).toList(),
                    p.getCreatedAt(),
                    p.getUpdatedAt()
            );
        }
    }

    public record ProductPageResponse(List<ProductResponse> data, int page, int pageSize, long total) {
        public static ProductPageResponse from(PageResult<Product> page, java.util.function.Function<Product, ProductResponse> mapper) {
            List<ProductResponse> data = page.data().stream().map(mapper).toList();
            return new ProductPageResponse(data, page.page(), page.pageSize(), page.total());
        }
    }

    public record AutocompleteResponse(String id, String description, String type, BigDecimal salePrice) {
        public static AutocompleteResponse from(Product p) {
            return new AutocompleteResponse(p.getProductId().toString(), p.getDescription(), p.getType().name(), p.getSalePrice());
        }
    }

    public record ImportFailureResponse(int line, String error) {
        public static ImportFailureResponse from(ProductImportReport.RowFailure f) {
            return new ImportFailureResponse(f.line(), f.error());
        }
    }

    public record ImportReportResponse(int total, int created, List<ImportFailureResponse> failed) {
        public static ImportReportResponse from(ProductImportReport report) {
            return new ImportReportResponse(
                    report.total(),
                    report.created(),
                    report.failed().stream().map(ImportFailureResponse::from).toList());
        }
    }
}
