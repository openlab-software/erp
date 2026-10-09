package software.openlab.customer.domain.customer;

import software.openlab.customer.domain.shared.BadRequestException;

/** INDIVIDUAL = pessoa física (CPF); COMPANY = pessoa jurídica (CNPJ). Immutable after creation. */
public enum CustomerType {
    INDIVIDUAL,
    COMPANY;

    public static CustomerType parse(String value) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException("error.field.required", "type");
        }
        try {
            return CustomerType.valueOf(value.trim());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("error.field.invalid", "type", value);
        }
    }
}
