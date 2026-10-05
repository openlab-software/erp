package software.openlab.catalog.domain.product;

import software.openlab.catalog.domain.shared.BadRequestException;
import software.openlab.catalog.domain.shared.Ulid;

/**
 * Strongly-typed public identifier for a {@link ProductBarcode}, in the form
 * {@code barcode_<ULID>} (e.g. {@code barcode_01J6Z3K9QZXJ6R6JYX8G0XJ0KQ}).
 */
public record ProductBarcodeId(String value) {

    private static final String PREFIX = "barcode";
    private static final String PREFIXED = PREFIX + "_";

    public ProductBarcodeId {
        if (value == null || !value.startsWith(PREFIXED) || !Ulid.isValid(value.substring(PREFIXED.length()))) {
            throw new BadRequestException("error.id.invalid", PREFIXED + "<ULID>");
        }
    }

    public static ProductBarcodeId generate() {
        return new ProductBarcodeId(PREFIXED + Ulid.generate());
    }

    public static ProductBarcodeId of(String raw) {
        return new ProductBarcodeId(raw);
    }

    @Override
    public String toString() {
        return value;
    }
}
