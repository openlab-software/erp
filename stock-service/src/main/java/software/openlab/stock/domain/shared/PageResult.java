package software.openlab.stock.domain.shared;

import java.util.List;

/** Domain-level paginated result — mirrors the {@code Listing_Page} response shape. */
public record PageResult<T>(List<T> data, int page, int pageSize, long total) {
}
