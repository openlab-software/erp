package software.openlab.crm.domain.stage;

import java.util.List;
import java.util.Optional;

public interface StageRepository {

    Optional<Stage> findById(StageId stageId);

    /** Ordered by position, then creation. */
    List<Stage> findAll();

    /** The first stage (lowest position) flagged as won, if any. */
    Optional<Stage> findWonStage();

    /** The first stage (lowest position): where new leads land by default. */
    Optional<Stage> findFirst();

    boolean existsByName(String name, StageId excludeStageId);

    boolean hasLeads(StageId stageId);

    void insert(Stage stage);

    void update(Stage stage);

    void deleteById(StageId stageId);
}
