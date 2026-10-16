package software.openlab.crm.domain.shared;

public class UnprocessableEntityException extends ApiException {

    public UnprocessableEntityException(String messageKey, Object... args) {
        super(422, messageKey, args);
    }
}
