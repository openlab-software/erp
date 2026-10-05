package software.openlab.stock.application.usecase.movement;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Instant;
import software.openlab.stock.application.usecase.stock.GetStockByIdUseCase;
import software.openlab.stock.domain.movement.StockMovement;
import software.openlab.stock.domain.movement.StockMovementRepository;
import software.openlab.stock.domain.movement.StockMovementType;
import software.openlab.stock.domain.shared.BadRequestException;
import software.openlab.stock.domain.shared.PageResult;
import software.openlab.stock.domain.shared.ProductId;
import software.openlab.stock.domain.stock.StockId;

/** GET /v1/stocks/{id}/movements (kardex) — Requirement 4 of stock-master-data. */
@ApplicationScoped
public class ListStockMovementsUseCase {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    @Inject
    StockMovementRepository stockMovementRepository;

    @Inject
    GetStockByIdUseCase getStockById;

    public PageResult<StockMovement> execute(StockId stockId, ProductId productId, StockMovementType type,
                                              Instant from, Instant to, int page, int pageSize) {
        getStockById.execute(stockId);

        int safePage = page < 1 ? DEFAULT_PAGE : page;
        int safePageSize = pageSize <= 0 ? DEFAULT_PAGE_SIZE : pageSize;
        if (safePageSize > MAX_PAGE_SIZE) {
            throw new BadRequestException("error.pageSize.max", MAX_PAGE_SIZE);
        }

        return stockMovementRepository.findByStock(stockId, productId, type, from, to, safePage, safePageSize);
    }
}
