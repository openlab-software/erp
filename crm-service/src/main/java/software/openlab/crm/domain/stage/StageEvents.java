package software.openlab.crm.domain.stage;

public final class StageEvents {

    public static final String CREATED = "stage.created";
    public static final String UPDATED = "stage.updated";
    public static final String DELETED = "stage.deleted";

    private StageEvents() {
    }

    public record StagePayload(String id, String name, int position, int probability, boolean won) {
        public static StagePayload of(Stage s) {
            return new StagePayload(s.getStageId().toString(), s.getName(), s.getPosition(), s.getProbability(),
                    s.isWon());
        }
    }

    public record StageDeletedPayload(String id) {
    }
}
