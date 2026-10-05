package software.openlab.stock.infra.i18n;

import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.openlab.stock.domain.shared.ApiException;

/**
 * Resolves message keys against the {@code i18n/messages_<locale>.properties} bundles
 * (pt-BR and en). The locale comes from the current request's {@code Accept-Language}
 * header and falls back to {@code app.i18n.default-locale} (pt-BR) when the header is
 * absent, unsupported, or there is no active request.
 */
@ApplicationScoped
public class MessageResolver {

    private static final String BUNDLE = "i18n.messages";
    private static final List<Locale> SUPPORTED = List.of(Locale.forLanguageTag("pt-BR"), Locale.ENGLISH);

    @ConfigProperty(name = "app.i18n.default-locale", defaultValue = "pt-BR")
    String defaultLocaleTag;

    @Inject
    Instance<RoutingContext> routingContext;

    public String resolve(ApiException exception) {
        return resolve(exception.getMessageKey(), exception.getArgs());
    }

    public String resolve(String key, Object... args) {
        return resolve(currentLocale(), key, args);
    }

    public String resolve(Locale locale, String key, Object... args) {
        String pattern;
        try {
            pattern = bundle(locale).getString(key);
        } catch (MissingResourceException e) {
            return key;
        }
        Object[] formatted = args == null ? new Object[0]
                : Arrays.stream(args).map(String::valueOf).toArray();
        return new MessageFormat(pattern, locale).format(formatted);
    }

    public Locale currentLocale() {
        try {
            if (routingContext.isResolvable()) {
                String header = routingContext.get().request().getHeader("Accept-Language");
                if (header != null && !header.isBlank()) {
                    for (Locale.LanguageRange range : Locale.LanguageRange.parse(header)) {
                        Locale match = supportedFor(range.getRange());
                        if (match != null) {
                            return match;
                        }
                    }
                }
            }
        } catch (RuntimeException e) {
            // no active request, or a malformed Accept-Language header: use the default
        }
        Locale configured = supportedFor(defaultLocaleTag);
        return configured != null ? configured : SUPPORTED.get(0);
    }

    /** Matches on language only, so "pt", "pt-PT" and "pt-BR" all resolve to the pt-BR bundle. */
    private static Locale supportedFor(String tag) {
        String language = Locale.forLanguageTag(tag).getLanguage();
        return SUPPORTED.stream().filter(l -> l.getLanguage().equals(language)).findFirst().orElse(null);
    }

    private static ResourceBundle bundle(Locale locale) {
        return ResourceBundle.getBundle(BUNDLE, locale, Thread.currentThread().getContextClassLoader(),
                ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES));
    }
}
