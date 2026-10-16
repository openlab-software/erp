package software.openlab.purchase.application.usecase.order;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import software.openlab.purchase.domain.order.PurchaseOrderRepository;
import software.openlab.purchase.domain.order.PurchaseOrderRepository.StateTotals;

/** Count and total per order state (the numbers behind a purchase dashboard). */
@ApplicationScoped
public class GetPurchaseSummaryUseCase {

    @Inject
    PurchaseOrderRepository repository;

    public List<StateTotals> execute() {
        return repository.totalsByState();
    }
}
