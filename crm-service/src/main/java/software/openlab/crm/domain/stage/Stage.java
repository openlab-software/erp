package software.openlab.crm.domain.stage;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.openlab.crm.domain.shared.BadRequestException;

/**
 * A column of the sales pipeline. {@code probability} is the default win probability a lead gets when it
 * enters the stage; {@code won} marks the stage that closes a lead as won (at most one per pipeline is used,
 * see {@link StageRepository#findWonStage()}).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Stage {

    private StageId stageId;
    private String name;
    private int position;
    private int probability;
    private boolean won;
    private Instant createdAt;
    private Instant updatedAt;

    public static Stage newStage(String name, int position, int probability, boolean won) {
        Stage stage = new Stage(StageId.generate(), null, 0, 0, false, Instant.now(), null);
        stage.apply(name, position, probability, won);
        return stage;
    }

    public void apply(String name, int position, int probability, boolean won) {
        if (probability < 0 || probability > 100) {
            throw new BadRequestException("error.stage.probability.range");
        }
        this.name = name;
        this.position = position;
        this.probability = won ? 100 : probability;
        this.won = won;
    }
}
