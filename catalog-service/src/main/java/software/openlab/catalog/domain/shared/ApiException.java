package software.openlab.catalog.domain.shared;

import lombok.Getter;

/**
 * Base type for domain/business errors that map directly to a standardized
 * {@code {"message": "..."}} HTTP error response.
 *
 * <p>The exception carries a message <em>key</em> plus arguments rather than
 * final text; the REST layer resolves it against the {@code i18n/messages}
 * bundles using the request's {@code Accept-Language}.
 */
@Getter
public abstract class ApiException extends RuntimeException {

    private final int status;
    private final String messageKey;
    private final transient Object[] args;

    protected ApiException(int status, String messageKey, Object... args) {
        super(messageKey);
        this.status = status;
        this.messageKey = messageKey;
        this.args = args;
    }
}
