package software.openlab.stock.domain.shared;

/**
 * Local, read-only reference to a Product aggregate owned by catalog-service. Never generated
 * here — always received (path/query params are not applicable, but RabbitMQ event payloads,
 * request bodies, and the catalog.products projection table all supply it) and only ever
 * parsed/wrapped via {@link #of(String)}.
 *
 * <p>The "prod" prefix matches catalog-service's own convention so ids round-trip identically
 * across the RabbitMQ boundary and the read-only cross-schema query — by convention only, the
 * two services share no code.
 */
public record ProductId(String value) {

    public static final String PREFIX = "prod";

    public ProductId {
        value = PrefixedId.parse(PREFIX, value);
    }

    public static ProductId of(String raw) {
        return new ProductId(raw);
    }

    @Override
    public String toString() {
        return value;
    }
}
