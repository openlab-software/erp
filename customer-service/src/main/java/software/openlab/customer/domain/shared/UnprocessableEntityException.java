package software.openlab.customer.domain.shared;

public class UnprocessableEntityException extends ApiException {

    public UnprocessableEntityException(String messageKey, Object... args) {
        super(422, messageKey, args);
    }
}
