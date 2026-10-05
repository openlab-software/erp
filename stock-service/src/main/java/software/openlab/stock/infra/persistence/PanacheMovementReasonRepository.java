package software.openlab.stock.infra.persistence;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import software.openlab.stock.domain.movementreason.MovementReason;
import software.openlab.stock.domain.movementreason.MovementReasonId;
import software.openlab.stock.domain.movementreason.MovementReasonRepository;
import software.openlab.stock.domain.shared.NotFoundException;
import software.openlab.stock.domain.shared.PageResult;

@ApplicationScoped
public class PanacheMovementReasonRepository implements MovementReasonRepository {

    @Override
    public PageResult<MovementReason> findAll(String q, int page, int pageSize) {
        var query = (q != null && !q.isBlank())
                ? MovementReasonEntity.find("lower(description) like ?1", Sort.descending("createdAt"),
                    "%" + q.toLowerCase() + "%")
                : MovementReasonEntity.findAll(Sort.descending("createdAt"));

        long total = query.count();
        List<MovementReasonEntity> entities = query.page(Page.of(page - 1, pageSize)).list();
        List<MovementReason> reasons = entities.stream().map(PanacheMovementReasonRepository::toDomain).toList();
        return new PageResult<>(reasons, page, pageSize, total);
    }

    @Override
    public Optional<MovementReason> findById(MovementReasonId reasonId) {
        return MovementReasonEntity.<MovementReasonEntity>find("publicId", reasonId.toString())
                .firstResultOptional()
                .map(PanacheMovementReasonRepository::toDomain);
    }

    @Override
    public boolean existsByDescriptionIgnoreCase(String description) {
        return MovementReasonEntity.count("lower(description) = ?1", description.toLowerCase()) > 0;
    }

    @Override
    public boolean existsByDescriptionIgnoreCaseExcluding(String description, MovementReasonId excludeReasonId) {
        return MovementReasonEntity.count("lower(description) = ?1 and publicId != ?2",
                description.toLowerCase(), excludeReasonId.toString()) > 0;
    }

    @Override
    public void insert(MovementReason reason) {
        MovementReasonEntity entity = new MovementReasonEntity();
        entity.publicId = reason.getReasonId().toString();
        entity.description = reason.getDescription();
        entity.createdAt = reason.getCreatedAt();
        entity.persist();
    }

    @Override
    public void update(MovementReason reason) {
        MovementReasonEntity entity = findEntityOrThrow(reason.getReasonId());
        entity.description = reason.getDescription();
    }

    @Override
    public void delete(MovementReasonId reasonId) {
        MovementReasonEntity entity = findEntityOrThrow(reasonId);
        entity.delete();
    }

    private MovementReasonEntity findEntityOrThrow(MovementReasonId reasonId) {
        return MovementReasonEntity.<MovementReasonEntity>find("publicId", reasonId.toString())
                .firstResultOptional()
                .orElseThrow(() -> new NotFoundException("error.movementReason.notFound"));
    }

    private static MovementReason toDomain(MovementReasonEntity e) {
        return MovementReason.reconstruct(MovementReasonId.of(e.publicId), e.description, e.createdAt);
    }
}
