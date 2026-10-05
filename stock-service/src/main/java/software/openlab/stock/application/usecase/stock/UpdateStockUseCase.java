package software.openlab.stock.application.usecase.stock;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.stock.domain.shared.ConflictException;
import software.openlab.stock.domain.shared.DomainEvent;
import software.openlab.stock.domain.stock.Stock;
import software.openlab.stock.domain.stock.StockEvents;
import software.openlab.stock.domain.stock.StockId;
import software.openlab.stock.domain.stock.StockRepository;
import software.openlab.stock.domain.stock.StockUpdatedPayload;

@ApplicationScoped
public class UpdateStockUseCase {

    @Inject
    StockRepository stockRepository;

    @Inject
    Event<DomainEvent> events;

    @Inject
    GetStockByIdUseCase getStockById;

    @Transactional
    public Stock execute(StockId stockId, String description) {
        Stock stock = getStockById.execute(stockId);

        String trimmed = description.trim();
        if (stockRepository.existsByDescriptionIgnoreCaseExcluding(trimmed, stockId)) {
            throw new ConflictException("error.stock.duplicate");
        }

        stock.rename(trimmed);
        stockRepository.update(stock);
        events.fire(new DomainEvent(StockEvents.UPDATED,
                new StockUpdatedPayload(stock.getStockId().toString(), stock.getDescription())));
        return stock;
    }
}
