package software.openlab.stock.application.usecase.stockcount;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.openlab.stock.application.usecase.stock.GetStockByIdUseCase;
import software.openlab.stock.domain.shared.BadRequestException;
import software.openlab.stock.domain.shared.PageResult;
import software.openlab.stock.domain.shared.ProductId;
import software.openlab.stock.domain.stock.StockId;
import software.openlab.stock.domain.stockcount.StockCount;
import software.openlab.stock.domain.stockcount.StockCountRepository;

/** GET /v1/stocks/{id}/counts — Requirement 7.5. */
@ApplicationScoped
public class ListStockCountsUseCase {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    @Inject
    StockCountRepository stockCountRepository;

    @Inject
    GetStockByIdUseCase getStockById;

    public PageResult<StockCount> execute(StockId stockId, ProductId productId, int page, int pageSize) {
        getStockById.execute(stockId);

        int safePage = page < 1 ? DEFAULT_PAGE : page;
        int safePageSize = pageSize <= 0 ? DEFAULT_PAGE_SIZE : pageSize;
        if (safePageSize > MAX_PAGE_SIZE) {
            throw new BadRequestException("error.pageSize.max", MAX_PAGE_SIZE);
        }

        return stockCountRepository.findByStock(stockId, productId, safePage, safePageSize);
    }
}
