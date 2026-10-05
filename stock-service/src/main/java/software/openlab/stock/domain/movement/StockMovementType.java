package software.openlab.stock.domain.movement;

/**
 * Per the {@code stock-master-data} Glossary. Only {@link #ENTRY}, {@link #EXIT} and
 * {@link #ADJUSTMENT} are accepted by {@code POST /v1/stocks/{id}/movements} (Requirement 3);
 * {@link #TRANSFER_IN}/{@link #TRANSFER_OUT} are generated internally by Reassignment
 * (Requirement 5) and never accepted directly from that endpoint.
 */
public enum StockMovementType {
    ENTRY,
    EXIT,
    ADJUSTMENT,
    TRANSFER_IN,
    TRANSFER_OUT
}
