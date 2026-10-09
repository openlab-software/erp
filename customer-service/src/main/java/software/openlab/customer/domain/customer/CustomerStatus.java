package software.openlab.customer.domain.customer;

import software.openlab.customer.domain.shared.BadRequestException;

public enum CustomerStatus {
    ACTIVE,
    INACTIVE;

    public static CustomerStatus parse(String value) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException("error.field.required", "status");
        }
        try {
            return CustomerStatus.valueOf(value.trim());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("error.field.invalid", "status", value);
        }
    }
}
