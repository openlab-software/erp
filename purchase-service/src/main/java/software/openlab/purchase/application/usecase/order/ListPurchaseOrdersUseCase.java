package software.openlab.purchase.application.usecase.order;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.openlab.purchase.domain.order.ReceiptStatus;
import software.openlab.purchase.domain.order.BillStatus;
import software.openlab.purchase.domain.order.OrderFilter;
import software.openlab.purchase.domain.order.OrderState;
import software.openlab.purchase.domain.order.PurchaseOrder;
import software.openlab.purchase.domain.order.PurchaseOrderRepository;
import software.openlab.purchase.domain.shared.PageResult;
import software.openlab.purchase.domain.shared.Refs;

@ApplicationScoped
public class ListPurchaseOrdersUseCase {

    @Inject
    PurchaseOrderRepository repository;

    public PageResult<PurchaseOrder> execute(String q, String state, String supplierId, String receiptStatus,
                                          String billStatus, Integer page, Integer pageSize) {
        OrderFilter filter = new OrderFilter(
                q,
                isBlank(state) ? null : OrderState.parse(state),
                Refs.optional("supplier", supplierId),
                isBlank(receiptStatus) ? null : ReceiptStatus.parse(receiptStatus),
                isBlank(billStatus) ? null : BillStatus.parse(billStatus));
        return repository.find(filter, PageResult.normalizePage(page), PageResult.normalizePageSize(pageSize));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
