package software.openlab.accounting.domain.shared;

public class ConflictException extends ApiException {

    public ConflictException(String messageKey, Object... args) {
        super(409, messageKey, args);
    }
}
