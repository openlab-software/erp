package software.openlab.catalog.domain.product;

import software.openlab.catalog.domain.shared.BadRequestException;

public enum BarcodeType {
    EAN13,
    EAN8,
    UPC,
    INTERNAL;

    public static BarcodeType parse(String value) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException("error.field.required", "type");
        }
        try {
            return BarcodeType.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("error.field.invalid", "type", value);
        }
    }
}
