package software.openlab.catalog.domain.product;

/**
 * Free-form {@code {name, value}} pair attached to a {@link Product}. Has no
 * lifecycle/id of its own — the whole set is replaced in block on every write
 * (see {@link ProductAttributeRepository#replaceAll}).
 */
public record ProductAttribute(String name, String value) {
}
