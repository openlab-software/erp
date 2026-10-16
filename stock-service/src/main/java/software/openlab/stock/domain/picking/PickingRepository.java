package software.openlab.stock.domain.picking;

import java.util.List;
import java.util.Optional;
import software.openlab.stock.domain.shared.PageResult;

public interface PickingRepository {

    Optional<Picking> findById(PickingId pickingId);

    PageResult<Picking> find(PickingFilter filter, int page, int pageSize);

    boolean existsByOrigin(PickingType type, String originType, String originId);

    /** Pickings of an originating document that are still DRAFT or CONFIRMED. */
    List<Picking> findOpenByOrigin(String originType, String originId);

    /** Next human reference of the type (IN/00001, OUT/00001, INT/00001). */
    String nextNumber(PickingType type);

    void insert(Picking picking);

    void update(Picking picking);

    void deleteById(PickingId pickingId);
}
