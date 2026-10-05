package software.openlab.catalog.domain.product;

import software.openlab.catalog.domain.shared.BadRequestException;

public enum ProductType {
    GOOD,
    SERVICE;

    public static ProductType parse(String value) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException("error.field.required", "type");
        }
        try {
            return ProductType.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("error.field.invalid", "type", value);
        }
    }
}
