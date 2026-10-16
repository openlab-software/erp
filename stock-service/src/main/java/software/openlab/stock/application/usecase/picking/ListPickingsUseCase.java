package software.openlab.stock.application.usecase.picking;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.openlab.stock.domain.picking.Picking;
import software.openlab.stock.domain.picking.PickingFilter;
import software.openlab.stock.domain.picking.PickingRepository;
import software.openlab.stock.domain.picking.PickingState;
import software.openlab.stock.domain.picking.PickingType;
import software.openlab.stock.domain.shared.BadRequestException;
import software.openlab.stock.domain.shared.PageResult;
import software.openlab.stock.domain.stock.StockId;

@ApplicationScoped
public class ListPickingsUseCase {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    @Inject
    PickingRepository repository;

    public PageResult<Picking> execute(String q, String type, String state, String originType, String originId,
                                       String stockId, int page, int pageSize) {
        int safePage = page < 1 ? 1 : page;
        int safePageSize = pageSize <= 0 ? DEFAULT_PAGE_SIZE : pageSize;
        if (safePageSize > MAX_PAGE_SIZE) {
            throw new BadRequestException("error.pageSize.max", MAX_PAGE_SIZE);
        }
        PickingFilter filter = new PickingFilter(
                q,
                isBlank(type) ? null : PickingType.parse(type),
                isBlank(state) ? null : PickingState.parse(state),
                originType,
                originId,
                isBlank(stockId) ? null : StockId.of(stockId));
        return repository.find(filter, safePage, safePageSize);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
