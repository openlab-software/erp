package software.openlab.crm.infra.persistence;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import software.openlab.crm.domain.stage.Stage;
import software.openlab.crm.domain.stage.StageId;
import software.openlab.crm.domain.stage.StageRepository;

@ApplicationScoped
public class StageRepositoryImpl implements StageRepository {

    private static final Sort ORDER = Sort.by("position").and("id");

    @Override
    public Optional<Stage> findById(StageId stageId) {
        return findEntity(stageId).map(this::toDomain);
    }

    @Override
    public List<Stage> findAll() {
        return StageEntity.<StageEntity>listAll(ORDER).stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<Stage> findWonStage() {
        return StageEntity.<StageEntity>find("won = true", ORDER).firstResultOptional().map(this::toDomain);
    }

    @Override
    public Optional<Stage> findFirst() {
        return StageEntity.<StageEntity>findAll(ORDER).firstResultOptional().map(this::toDomain);
    }

    @Override
    public boolean existsByName(String name, StageId excludeStageId) {
        if (excludeStageId == null) {
            return StageEntity.count("lower(name) = ?1", name.toLowerCase()) > 0;
        }
        return StageEntity.count("lower(name) = ?1 and publicId <> ?2", name.toLowerCase(),
                excludeStageId.toString()) > 0;
    }

    @Override
    public boolean hasLeads(StageId stageId) {
        return LeadEntity.count("stage.publicId", stageId.toString()) > 0;
    }

    @Override
    public void insert(Stage stage) {
        StageEntity entity = new StageEntity();
        entity.publicId = stage.getStageId().toString();
        entity.createdAt = stage.getCreatedAt();
        copyToEntity(stage, entity);
        StageEntity.persist(entity);
    }

    @Override
    public void update(Stage stage) {
        StageEntity entity = findEntity(stage.getStageId())
                .orElseThrow(() -> new IllegalStateException("stage not found: " + stage.getStageId()));
        copyToEntity(stage, entity);
        entity.updatedAt = Instant.now();
    }

    @Override
    public void deleteById(StageId stageId) {
        StageEntity.delete("publicId", stageId.toString());
    }

    private Optional<StageEntity> findEntity(StageId stageId) {
        return StageEntity.<StageEntity>find("publicId", stageId.toString()).firstResultOptional();
    }

    private void copyToEntity(Stage s, StageEntity e) {
        e.name = s.getName();
        e.position = s.getPosition();
        e.probability = s.getProbability();
        e.won = s.isWon();
    }

    private Stage toDomain(StageEntity e) {
        return new Stage(StageId.of(e.publicId), e.name, e.position, e.probability, e.won, e.createdAt, e.updatedAt);
    }
}
