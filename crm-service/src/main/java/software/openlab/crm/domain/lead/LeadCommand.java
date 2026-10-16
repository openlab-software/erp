package software.openlab.crm.domain.lead;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import software.openlab.crm.domain.shared.BadRequestException;
import software.openlab.crm.domain.shared.Fields;
import software.openlab.crm.domain.shared.Refs;

/** Editable data of a lead; {@link #validated()} normalizes it. */
public record LeadCommand(
        String name,
        String customerId,
        String contactName,
        String companyName,
        String email,
        String phone,
        BigDecimal expectedRevenue,
        Integer probability,
        int priority,
        LocalDate expectedClosing,
        String description,
        String assignedTo,
        Set<String> tags) {

    public static final int MAX_PRIORITY = 3;

    public LeadCommand validated() {
        String cleanName = Fields.requireNonBlank(name, "name");
        BigDecimal revenue = expectedRevenue == null ? BigDecimal.ZERO : expectedRevenue;
        if (revenue.signum() < 0) {
            throw new BadRequestException("error.field.invalid", "expected_revenue", expectedRevenue);
        }
        if (probability != null && (probability < 0 || probability > 100)) {
            throw new BadRequestException("error.field.invalid", "probability", probability);
        }
        if (priority < 0 || priority > MAX_PRIORITY) {
            throw new BadRequestException("error.field.invalid", "priority", priority);
        }
        String mail = blankToNull(email);
        if (mail != null && !mail.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new BadRequestException("error.lead.email.invalid", email);
        }
        Set<String> cleanTags = new LinkedHashSet<>();
        if (tags != null) {
            for (String tag : tags) {
                if (tag != null && !tag.isBlank()) {
                    cleanTags.add(tag.trim().toLowerCase());
                }
            }
        }
        return new LeadCommand(cleanName, Refs.optional("customer", customerId), blankToNull(contactName),
                blankToNull(companyName), mail, blankToNull(phone), revenue.setScale(2, RoundingMode.HALF_UP),
                probability, priority, expectedClosing, blankToNull(description), blankToNull(assignedTo),
                cleanTags);
    }

    private static String blankToNull(String v) {
        return v == null || v.isBlank() ? null : v.trim();
    }
}
