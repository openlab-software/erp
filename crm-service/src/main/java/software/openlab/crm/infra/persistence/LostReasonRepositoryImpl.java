package software.openlab.crm.infra.persistence;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import software.openlab.crm.domain.lostreason.LostReason;
import software.openlab.crm.domain.lostreason.LostReasonId;
import software.openlab.crm.domain.lostreason.LostReasonRepository;

@ApplicationScoped
public class LostReasonRepositoryImpl implements LostReasonRepository {

    @Override
    public Optional<LostReason> findById(LostReasonId id) {
        return findEntity(id).map(this::toDomain);
    }

    @Override
    public List<LostReason> findAll() {
        return LostReasonEntity.<LostReasonEntity>listAll(Sort.by("name")).stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsByName(String name, LostReasonId excludeId) {
        if (excludeId == null) {
            return LostReasonEntity.count("lower(name) = ?1", name.toLowerCase()) > 0;
        }
        return LostReasonEntity.count("lower(name) = ?1 and publicId <> ?2", name.toLowerCase(),
                excludeId.toString()) > 0;
    }

    @Override
    public boolean isUsed(LostReasonId id) {
        return LeadEntity.count("lostReason.publicId", id.toString()) > 0;
    }

    @Override
    public void insert(LostReason reason) {
        LostReasonEntity entity = new LostReasonEntity();
        entity.publicId = reason.getLostReasonId().toString();
        entity.createdAt = reason.getCreatedAt();
        entity.name = reason.getName();
        entity.active = reason.isActive();
        LostReasonEntity.persist(entity);
    }

    @Override
    public void update(LostReason reason) {
        LostReasonEntity entity = findEntity(reason.getLostReasonId())
                .orElseThrow(() -> new IllegalStateException("lost reason not found: " + reason.getLostReasonId()));
        entity.name = reason.getName();
        entity.active = reason.isActive();
        entity.updatedAt = Instant.now();
    }

    @Override
    public void deleteById(LostReasonId id) {
        LostReasonEntity.delete("publicId", id.toString());
    }

    private Optional<LostReasonEntity> findEntity(LostReasonId id) {
        return LostReasonEntity.<LostReasonEntity>find("publicId", id.toString()).firstResultOptional();
    }

    private LostReason toDomain(LostReasonEntity e) {
        return new LostReason(LostReasonId.of(e.publicId), e.name, e.active, e.createdAt, e.updatedAt);
    }
}
