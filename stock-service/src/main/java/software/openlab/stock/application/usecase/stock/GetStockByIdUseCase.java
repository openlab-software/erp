package software.openlab.stock.application.usecase.stock;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.openlab.stock.domain.shared.NotFoundException;
import software.openlab.stock.domain.stock.Stock;
import software.openlab.stock.domain.stock.StockId;
import software.openlab.stock.domain.stock.StockRepository;

@ApplicationScoped
public class GetStockByIdUseCase {

    @Inject
    StockRepository stockRepository;

    public Stock execute(StockId stockId) {
        return stockRepository.findById(stockId)
                .orElseThrow(() -> new NotFoundException("error.stock.notFound"));
    }
}
