package software.openlab.customer.domain.customer;

import software.openlab.customer.domain.shared.BadRequestException;

/**
 * CPF / CNPJ normalization and check-digit validation. Punctuation is accepted on input
 * ({@code 123.456.789-09}); the canonical form is digits only.
 */
public final class Document {

    private Document() {
    }

    /** Validates {@code raw} as the document kind that matches {@code type} and returns its digits. */
    public static String normalize(CustomerType type, String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BadRequestException("error.field.blank", "document");
        }
        String digits = raw.replaceAll("\\D", "");
        boolean valid = type == CustomerType.INDIVIDUAL ? isValidCpf(digits) : isValidCnpj(digits);
        if (!valid) {
            throw new BadRequestException(
                    type == CustomerType.INDIVIDUAL ? "error.customer.document.cpf" : "error.customer.document.cnpj",
                    raw.trim());
        }
        return digits;
    }

    static boolean isValidCpf(String d) {
        if (d.length() != 11 || allSame(d)) {
            return false;
        }
        return cpfDigit(d, 9) == d.charAt(9) - '0' && cpfDigit(d, 10) == d.charAt(10) - '0';
    }

    static boolean isValidCnpj(String d) {
        if (d.length() != 14 || allSame(d)) {
            return false;
        }
        int[] first = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] second = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        return cnpjDigit(d, first) == d.charAt(12) - '0' && cnpjDigit(d, second) == d.charAt(13) - '0';
    }

    /** CPF check digit computed over the first {@code length} digits with weights {@code length+1 .. 2}. */
    private static int cpfDigit(String d, int length) {
        int sum = 0;
        for (int i = 0; i < length; i++) {
            sum += (d.charAt(i) - '0') * (length + 1 - i);
        }
        int rest = (sum * 10) % 11;
        return rest == 10 ? 0 : rest;
    }

    private static int cnpjDigit(String d, int[] weights) {
        int sum = 0;
        for (int i = 0; i < weights.length; i++) {
            sum += (d.charAt(i) - '0') * weights[i];
        }
        int rest = sum % 11;
        return rest < 2 ? 0 : 11 - rest;
    }

    private static boolean allSame(String d) {
        return d.chars().distinct().count() == 1;
    }
}
