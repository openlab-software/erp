package software.openlab.stock.domain.shared;

/** Maps to HTTP 409 with body {@code {"message": ...}}. */
public class ConflictException extends ApiException {

    public ConflictException(String messageKey, Object... args) {
        super(messageKey, args);
    }
}
