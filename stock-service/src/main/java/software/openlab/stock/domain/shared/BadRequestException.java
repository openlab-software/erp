package software.openlab.stock.domain.shared;

/** Maps to HTTP 400 with body {@code {"message": ...}}. */
public class BadRequestException extends ApiException {

    public BadRequestException(String messageKey, Object... args) {
        super(messageKey, args);
    }
}
