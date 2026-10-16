package software.openlab.crm.domain.shared;

public class ConflictException extends ApiException {

    public ConflictException(String messageKey, Object... args) {
        super(409, messageKey, args);
    }
}
