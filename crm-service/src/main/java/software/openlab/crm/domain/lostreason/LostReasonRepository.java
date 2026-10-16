package software.openlab.crm.domain.lostreason;

import java.util.List;
import java.util.Optional;

public interface LostReasonRepository {

    Optional<LostReason> findById(LostReasonId id);

    List<LostReason> findAll();

    boolean existsByName(String name, LostReasonId excludeId);

    boolean isUsed(LostReasonId id);

    void insert(LostReason reason);

    void update(LostReason reason);

    void deleteById(LostReasonId id);
}
