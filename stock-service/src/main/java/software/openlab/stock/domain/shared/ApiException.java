package software.openlab.stock.domain.shared;

import lombok.Getter;

/**
 * Base type for domain/business errors that carry an HTTP-status-shaped meaning.
 *
 * <p>The exception carries a message <em>key</em> plus arguments rather than
 * final text; the REST layer resolves it against the {@code i18n/messages}
 * bundles using the request's {@code Accept-Language}.
 */
@Getter
public abstract class ApiException extends RuntimeException {

    private final String messageKey;
    private final transient Object[] args;

    protected ApiException(String messageKey, Object... args) {
        super(messageKey);
        this.messageKey = messageKey;
        this.args = args;
    }
}
