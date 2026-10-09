package software.openlab.customer.domain.shared;

import java.util.List;

/**
 * A page of results, mirroring the spec's {@code Listing_Page} shape:
 * {@code { "data": [...], "page": <int>, "page_size": <int>, "total": <int> }}.
 */
public record PageResult<T>(List<T> data, int page, int pageSize, long total) {

    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    public static int normalizePage(Integer page) {
        if (page == null || page < 1) {
            return DEFAULT_PAGE;
        }
        return page;
    }

    public static int normalizePageSize(Integer pageSize) {
        if (pageSize == null) {
            return DEFAULT_PAGE_SIZE;
        }
        if (pageSize > MAX_PAGE_SIZE) {
            throw new BadRequestException("error.pageSize.max", MAX_PAGE_SIZE);
        }
        if (pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return pageSize;
    }
}
