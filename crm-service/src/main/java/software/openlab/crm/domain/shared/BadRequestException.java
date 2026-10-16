package software.openlab.crm.domain.shared;

public class BadRequestException extends ApiException {

    public BadRequestException(String messageKey, Object... args) {
        super(400, messageKey, args);
    }
}
