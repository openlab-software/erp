package software.openlab.stock.domain.shared;

/** Maps to HTTP 404 with body {@code {"message": ...}}. */
public class NotFoundException extends ApiException {

    public NotFoundException(String messageKey, Object... args) {
        super(messageKey, args);
    }
}
