package software.openlab.accounting.domain.shared;

public class NotFoundException extends ApiException {

    public NotFoundException(String messageKey, Object... args) {
        super(404, messageKey, args);
    }
}
