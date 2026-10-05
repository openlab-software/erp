package software.openlab.stock.domain.movementreason;

import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class MovementReason {

    private final MovementReasonId reasonId;
    private String description;
    private final Instant createdAt;

    public static MovementReason create(String description) {
        return new MovementReason(MovementReasonId.generate(), description, Instant.now());
    }

    /** Rebuilds a MovementReason already persisted — used when loading from storage. */
    public static MovementReason reconstruct(MovementReasonId reasonId, String description, Instant createdAt) {
        return new MovementReason(reasonId, description, createdAt);
    }

    public void rename(String newDescription) {
        this.description = newDescription;
    }
}
