package software.openlab.catalog.domain.product;

import java.util.Set;
import software.openlab.catalog.domain.shared.BadRequestException;
import software.openlab.catalog.domain.shared.ConflictException;

public enum ProductStatus {
    DRAFT,
    PUBLISHED,
    ACTIVE,
    INACTIVE;

    public static ProductStatus parse(String value) {
        try {
            return ProductStatus.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BadRequestException("error.field.invalid", "status", value);
        }
    }

    private static final Set<String> ALLOWED_TRANSITIONS = Set.of(
            DRAFT + "->" + PUBLISHED,
            PUBLISHED + "->" + INACTIVE,
            INACTIVE + "->" + PUBLISHED
    );

    public void requireTransitionTo(ProductStatus target) {
        if (!ALLOWED_TRANSITIONS.contains(this + "->" + target)) {
            throw new ConflictException("error.product.statusTransition", this, target);
        }
    }
}
