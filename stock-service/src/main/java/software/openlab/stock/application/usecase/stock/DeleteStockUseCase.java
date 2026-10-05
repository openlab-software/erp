package software.openlab.stock.application.usecase.stock;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.openlab.stock.domain.shared.ConflictException;
import software.openlab.stock.domain.shared.DomainEvent;
import software.openlab.stock.domain.stock.StockDeletedPayload;
import software.openlab.stock.domain.stock.StockEvents;
import software.openlab.stock.domain.stock.StockId;
import software.openlab.stock.domain.stock.StockRepository;

@ApplicationScoped
public class DeleteStockUseCase {

    @Inject
    StockRepository stockRepository;

    @Inject
    Event<DomainEvent> events;

    @Inject
    GetStockByIdUseCase getStockById;

    @Transactional
    public void execute(StockId stockId) {
        getStockById.execute(stockId);

        if (stockRepository.hasActiveItems(stockId)) {
            throw new ConflictException("error.stock.hasBalance");
        }
        if (stockRepository.hasReservedItems(stockId)) {
            throw new ConflictException("error.stock.hasReserved");
        }
        if (stockRepository.isReferencedInAnyReassignment(stockId)) {
            throw new ConflictException("error.stock.inReassignment");
        }

        stockRepository.deleteCascade(stockId);
        events.fire(new DomainEvent(StockEvents.DELETED, new StockDeletedPayload(stockId.toString())));
    }
}
