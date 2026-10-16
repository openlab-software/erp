package software.openlab.stock.domain.shared;

/** Remembers which incoming integration events were already applied (brokers deliver at least once). */
public interface ProcessedEventRepository {

    /** Records the key and returns true the first time; false when it was already recorded. */
    boolean firstTime(String eventKey);
}
