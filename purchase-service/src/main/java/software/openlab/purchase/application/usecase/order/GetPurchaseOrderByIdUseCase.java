package software.openlab.purchase.application.usecase.order;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.openlab.purchase.domain.order.PurchaseOrder;
import software.openlab.purchase.domain.order.PurchaseOrderId;
import software.openlab.purchase.domain.order.PurchaseOrderRepository;
import software.openlab.purchase.domain.shared.NotFoundException;

@ApplicationScoped
public class GetPurchaseOrderByIdUseCase {

    @Inject
    PurchaseOrderRepository repository;

    public PurchaseOrder execute(PurchaseOrderId orderId) {
        return repository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("error.order.notFound", orderId));
    }
}
