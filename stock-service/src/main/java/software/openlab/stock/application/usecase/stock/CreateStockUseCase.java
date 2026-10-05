package software.openlab.stock.application.usecase.stock;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.stock.domain.shared.ConflictException;
import software.openlab.stock.domain.shared.DomainEvent;
import software.openlab.stock.domain.stock.Stock;
import software.openlab.stock.domain.stock.StockCreatedPayload;
import software.openlab.stock.domain.stock.StockEvents;
import software.openlab.stock.domain.stock.StockRepository;

@ApplicationScoped
public class CreateStockUseCase {

    @Inject
    StockRepository stockRepository;

    @Inject
    Event<DomainEvent> events;

    @Transactional
    public Stock execute(String description) {
        String trimmed = description.trim();
        if (stockRepository.existsByDescriptionIgnoreCase(trimmed)) {
            throw new ConflictException("error.stock.duplicate");
        }

        Stock stock = Stock.create(trimmed);
        stockRepository.insert(stock);
        events.fire(new DomainEvent(
                StockEvents.CREATED,
                new StockCreatedPayload(stock.getStockId().toString(), stock.getDescription())
        ));
        return stock;
    }
}
