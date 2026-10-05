package software.openlab.stock.application.usecase.stockitem;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.openlab.stock.application.usecase.stock.GetStockByIdUseCase;
import software.openlab.stock.domain.shared.BadRequestException;
import software.openlab.stock.domain.shared.PageResult;
import software.openlab.stock.domain.shared.ProductId;
import software.openlab.stock.domain.stock.StockId;
import software.openlab.stock.domain.stockitem.StockItem;
import software.openlab.stock.domain.stockitem.StockItemRepository;

@ApplicationScoped
public class ListStockItemsUseCase {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    @Inject
    StockItemRepository stockItemRepository;

    @Inject
    GetStockByIdUseCase getStockById;

    public PageResult<StockItem> execute(StockId stockId, ProductId productId, Boolean belowMin, Boolean aboveMax,
                                          int page, int pageSize) {
        getStockById.execute(stockId);

        if (Boolean.TRUE.equals(belowMin) && Boolean.TRUE.equals(aboveMax)) {
            throw new BadRequestException("error.stockItem.filterConflict");
        }

        int safePage = page < 1 ? DEFAULT_PAGE : page;
        int safePageSize = pageSize <= 0 ? DEFAULT_PAGE_SIZE : pageSize;
        if (safePageSize > MAX_PAGE_SIZE) {
            throw new BadRequestException("error.pageSize.max", MAX_PAGE_SIZE);
        }

        return stockItemRepository.findByStock(stockId, productId, belowMin, aboveMax, safePage, safePageSize);
    }
}
