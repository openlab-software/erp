package software.openlab.stock.domain.movementreason;

import java.util.Optional;
import software.openlab.stock.domain.shared.PageResult;

public interface MovementReasonRepository {

    PageResult<MovementReason> findAll(String q, int page, int pageSize);

    Optional<MovementReason> findById(MovementReasonId reasonId);

    boolean existsByDescriptionIgnoreCase(String description);

    boolean existsByDescriptionIgnoreCaseExcluding(String description, MovementReasonId excludeReasonId);

    void insert(MovementReason reason);

    void update(MovementReason reason);

    /**
     * Hard-deletes the reason. Requirement 2.4 (block deletion when a Stock_Movement
     * references this reason) is out of scope here — Stock_Movement does not exist yet and is
     * introduced by a later task.
     */
    void delete(MovementReasonId reasonId);
}
