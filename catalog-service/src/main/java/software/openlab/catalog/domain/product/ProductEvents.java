package software.openlab.catalog.domain.product;

import java.math.BigDecimal;
import java.util.List;

public final class ProductEvents {

    public static final String CREATED = "product.created";
    public static final String UPDATED = "product.updated";
    public static final String DELETED = "product.deleted";
    public static final String PRICE_CHANGED = "product.price_changed";

    private ProductEvents() {
    }

    public record ProductCreatedPayload(String id, String description, String type, String unitOfMeasureId,
                                         String categoryId, String brandId, String defaultSupplierId) {
    }

    public record ProductUpdatedPayload(String id, String description, String shortDescription, String type,
                                         String unitOfMeasureId, String categoryId, String status,
                                         String brandId, String defaultSupplierId) {
    }

    public record ProductDeletedPayload(String id) {
    }

    public record ProductPriceChangedPayload(String id, BigDecimal salePrice, BigDecimal costPrice) {
    }

    public record BarcodeItem(String barcodeId, String code, String type) {
        public static BarcodeItem from(ProductBarcode b) {
            return new BarcodeItem(b.getBarcodeId().toString(), b.getCode(), b.getType().name());
        }
    }

    public record ImageItem(String imageId, String url, boolean isPrimary) {
        public static ImageItem from(ProductImage i) {
            return new ImageItem(i.getImageId().toString(), i.getUrl(), i.isPrimary());
        }
    }

    public record ProductAttributesUpdatedPayload(String id, List<ProductAttribute> attributes) {
    }

    public record ProductBarcodesUpdatedPayload(String id, List<BarcodeItem> barcodes) {
    }

    public record ProductImagesUpdatedPayload(String id, List<ImageItem> images) {
    }

    public static ProductUpdatedPayload updatedPayloadOf(Product p) {
        return new ProductUpdatedPayload(
                p.getProductId().toString(),
                p.getDescription(),
                p.getShortDescription(),
                p.getType().name(),
                p.getUnitOfMeasureId().toString(),
                p.getCategoryId().toString(),
                p.getStatus().name(),
                p.getBrandId() != null ? p.getBrandId().toString() : null,
                p.getDefaultSupplierId() != null ? p.getDefaultSupplierId().toString() : null);
    }
}
