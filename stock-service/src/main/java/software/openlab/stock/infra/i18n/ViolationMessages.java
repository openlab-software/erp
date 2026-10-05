package software.openlab.stock.infra.i18n;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.lang.annotation.Annotation;

/** Turns bean-validation violations into localized messages from the {@code validation.*} keys. */
@ApplicationScoped
public class ViolationMessages {

    @Inject
    MessageResolver messages;

    public String summary() {
        return messages.resolve("validation.summary");
    }

    public String messageFor(ConstraintViolation<?> violation) {
        Annotation annotation = violation.getConstraintDescriptor().getAnnotation();
        Class<? extends Annotation> type = annotation.annotationType();
        if (type == NotBlank.class || type == NotNull.class || type == NotEmpty.class) {
            return messages.resolve("validation.required");
        }
        if (type == Min.class) {
            return messages.resolve("validation.min", ((Min) annotation).value());
        }
        if (type == Max.class) {
            return messages.resolve("validation.max", ((Max) annotation).value());
        }
        if (type == Positive.class) {
            return messages.resolve("validation.positive");
        }
        if (type == PositiveOrZero.class) {
            return messages.resolve("validation.positiveOrZero");
        }
        if (type == Size.class) {
            Size size = (Size) annotation;
            boolean bounded = size.max() < Integer.MAX_VALUE;
            if (size.min() > 0 && bounded) {
                return messages.resolve("validation.size.range", size.min(), size.max());
            }
            return bounded ? messages.resolve("validation.size.max", size.max())
                    : messages.resolve("validation.size.min", size.min());
        }
        return violation.getMessage();
    }
}
