package software.openlab.sales.application.usecase.order;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.openlab.sales.domain.order.SalesOrder;
import software.openlab.sales.domain.order.SalesOrderId;
import software.openlab.sales.domain.order.SalesOrderRepository;
import software.openlab.sales.domain.shared.NotFoundException;

@ApplicationScoped
public class GetSalesOrderByIdUseCase {

    @Inject
    SalesOrderRepository repository;

    public SalesOrder execute(SalesOrderId orderId) {
        return repository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("error.order.notFound", orderId));
    }
}
