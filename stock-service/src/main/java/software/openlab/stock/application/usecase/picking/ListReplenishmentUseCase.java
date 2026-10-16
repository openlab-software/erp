package software.openlab.stock.application.usecase.picking;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.openlab.stock.domain.shared.BadRequestException;
import software.openlab.stock.domain.shared.PageResult;
import software.openlab.stock.domain.stockitem.StockItem;
import software.openlab.stock.domain.stockitem.StockItemRepository;

/**
 * Reordering rules at a glance: the items whose balance fell below the minimum, in any stock. The minimum and
 * maximum of an item are its reordering rule; suggested quantity = (maximum, or the minimum when there is no
 * maximum) - current balance.
 */
@ApplicationScoped
public class ListReplenishmentUseCase {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    @Inject
    StockItemRepository stockItemRepository;

    public PageResult<StockItem> execute(int page, int pageSize) {
        int safePage = page < 1 ? 1 : page;
        int safePageSize = pageSize <= 0 ? DEFAULT_PAGE_SIZE : pageSize;
        if (safePageSize > MAX_PAGE_SIZE) {
            throw new BadRequestException("error.pageSize.max", MAX_PAGE_SIZE);
        }
        return stockItemRepository.findBelowMinimum(safePage, safePageSize);
    }

    public static int suggestedQuantity(StockItem item) {
        int target = item.maxValue() != null ? item.maxValue() : item.minValue() != null ? item.minValue() : 0;
        return Math.max(target - item.currentValue(), 0);
    }
}
