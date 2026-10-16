package software.openlab.sales.application.usecase.order;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import software.openlab.sales.domain.order.SalesOrderRepository;
import software.openlab.sales.domain.order.SalesOrderRepository.StateTotals;

/** Count and total per order state (the numbers behind a sales dashboard). */
@ApplicationScoped
public class GetSalesSummaryUseCase {

    @Inject
    SalesOrderRepository repository;

    public List<StateTotals> execute() {
        return repository.totalsByState();
    }
}
