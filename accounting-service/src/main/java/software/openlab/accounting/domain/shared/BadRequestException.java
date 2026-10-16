package software.openlab.accounting.domain.shared;

public class BadRequestException extends ApiException {

    public BadRequestException(String messageKey, Object... args) {
        super(400, messageKey, args);
    }
}
