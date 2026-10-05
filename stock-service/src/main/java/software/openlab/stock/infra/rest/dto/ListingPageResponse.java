package software.openlab.stock.infra.rest.dto;

import java.util.List;

/** {@code Listing_Page}: {"data": [...], "page": <int>, "page_size": <int>, "total": <int>}. */
public record ListingPageResponse<T>(List<T> data, int page, int pageSize, long total) {
}
