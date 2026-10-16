package software.openlab.stock.application.usecase.picking;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.openlab.stock.domain.stock.Stock;
import software.openlab.stock.domain.stock.StockId;
import software.openlab.stock.domain.stock.StockRepository;

/**
 * Where automatic pickings (from sales / purchase orders) take goods from or put them in:
 * {@code stock.integration.default-stock} when configured, otherwise the first stock created. With no stock at all the
 * picking is created without one and a user picks the stock before validating it.
 */
@ApplicationScoped
public class DefaultStockResolver {

    @ConfigProperty(name = "stock.integration.default-stock")
    Optional<String> configured;

    @Inject
    StockRepository stockRepository;

    public Optional<StockId> resolve() {
        if (configured.isPresent() && !configured.get().isBlank()) {
            return stockRepository.findById(StockId.of(configured.get().trim())).map(Stock::getStockId);
        }
        return stockRepository.findFirstCreated().map(Stock::getStockId);
    }
}
