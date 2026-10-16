package software.openlab.purchase.infra.rest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import software.openlab.purchase.domain.order.OrderCommand;
import software.openlab.purchase.domain.order.OrderLine;
import software.openlab.purchase.domain.order.PurchaseOrder;
import software.openlab.purchase.domain.order.PurchaseOrderRepository.StateTotals;
import software.openlab.purchase.domain.shared.PageResult;

public final class PurchaseOrderDTOs {

    private PurchaseOrderDTOs() {
    }

    public record LineRequest(
            @NotBlank String productId,
            String sku,
            @NotBlank String description,
            @NotNull @Positive BigDecimal quantity,
            @PositiveOrZero BigDecimal unitPrice,
            @PositiveOrZero BigDecimal discountPercent,
            @PositiveOrZero BigDecimal taxPercent,
            String productType
    ) {
        OrderLine toDomain() {
            return new OrderLine(productId, sku, description, quantity, unitPrice, discountPercent, taxPercent, null,
                    productType);
        }
    }

    /** Whole-RFQ body for create and update (PUT replaces the lines). */
    public record PurchaseOrderRequest(
            @NotBlank String supplierId,
            @NotBlank String supplierName,
            String origin,
            String currency,
            LocalDate expectedDate,
            String shipToAddress,
            String buyer,
            String note,
            @Valid @Size(max = OrderCommand.MAX_LINES) List<LineRequest> lines
    ) {
        public OrderCommand toCommand() {
            List<OrderLine> mapped = lines == null ? List.of() : lines.stream().map(LineRequest::toDomain).toList();
            return new OrderCommand(supplierId, supplierName, origin, currency, expectedDate, shipToAddress,
                    buyer, note, mapped);
        }
    }

    public record LineResponse(String productId, String sku, String description, BigDecimal quantity,
                               BigDecimal unitPrice, BigDecimal discountPercent, BigDecimal taxPercent,
                               BigDecimal receivedQuantity, String productType, BigDecimal subtotal,
                               BigDecimal total) {
        static LineResponse from(OrderLine l) {
            return new LineResponse(l.productId(), l.sku(), l.description(), l.quantity(), l.unitPrice(),
                    l.discountPercent(), l.taxPercent(), l.receivedQuantity(), l.productType(), l.subtotal(),
                    l.total());
        }
    }

    public record PurchaseOrderResponse(
            String purchaseOrderId,
            String number,
            String state,
            String supplierId,
            String supplierName,
            String origin,
            String currency,
            LocalDate expectedDate,
            String shipToAddress,
            String buyer,
            String note,
            String receiptStatus,
            String billStatus,
            BigDecimal untaxedAmount,
            BigDecimal taxAmount,
            BigDecimal totalAmount,
            List<LineResponse> lines,
            Instant confirmedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static PurchaseOrderResponse from(PurchaseOrder o) {
            return new PurchaseOrderResponse(o.getOrderId().toString(), o.getNumber(), o.getState().name(),
                    o.getSupplierId(), o.getSupplierName(), o.getOrigin(), o.getCurrency(), o.getExpectedDate(),
                    o.getShipToAddress(), o.getBuyer(), o.getNote(), o.getReceiptStatus().name(),
                    o.getBillStatus().name(), o.untaxedAmount(), o.taxAmount(), o.totalAmount(),
                    o.getLines().stream().map(LineResponse::from).toList(), o.getConfirmedAt(), o.getCreatedAt(),
                    o.getUpdatedAt());
        }
    }

    public record PurchaseOrderPageResponse(List<PurchaseOrderResponse> data, int page, int pageSize, long total) {
        public static PurchaseOrderPageResponse from(PageResult<PurchaseOrder> page) {
            return new PurchaseOrderPageResponse(page.data().stream().map(PurchaseOrderResponse::from).toList(),
                    page.page(), page.pageSize(), page.total());
        }
    }

    public record StateTotalsResponse(String state, long count, BigDecimal totalAmount) {
        static StateTotalsResponse from(StateTotals t) {
            return new StateTotalsResponse(t.state().name(), t.count(), t.totalAmount());
        }
    }

    public record PurchaseSummaryResponse(List<StateTotalsResponse> data) {
        public static PurchaseSummaryResponse from(List<StateTotals> totals) {
            return new PurchaseSummaryResponse(totals.stream().map(StateTotalsResponse::from).toList());
        }
    }
}
