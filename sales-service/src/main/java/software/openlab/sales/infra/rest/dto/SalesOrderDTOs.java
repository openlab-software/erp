package software.openlab.sales.infra.rest.dto;

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
import software.openlab.sales.domain.order.OrderCommand;
import software.openlab.sales.domain.order.OrderLine;
import software.openlab.sales.domain.order.SalesOrder;
import software.openlab.sales.domain.order.SalesOrderRepository.StateTotals;
import software.openlab.sales.domain.shared.PageResult;

public final class SalesOrderDTOs {

    private SalesOrderDTOs() {
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

    /** Whole-quotation body for create and update (PUT replaces the lines). */
    public record SalesOrderRequest(
            @NotBlank String customerId,
            @NotBlank String customerName,
            String leadId,
            String currency,
            LocalDate validUntil,
            String deliveryAddress,
            String salesperson,
            String note,
            @Valid @Size(max = OrderCommand.MAX_LINES) List<LineRequest> lines
    ) {
        public OrderCommand toCommand() {
            List<OrderLine> mapped = lines == null ? List.of() : lines.stream().map(LineRequest::toDomain).toList();
            return new OrderCommand(customerId, customerName, leadId, currency, validUntil, deliveryAddress,
                    salesperson, note, mapped);
        }
    }

    public record LineResponse(String productId, String sku, String description, BigDecimal quantity,
                               BigDecimal unitPrice, BigDecimal discountPercent, BigDecimal taxPercent,
                               BigDecimal deliveredQuantity, String productType, BigDecimal subtotal,
                               BigDecimal total) {
        static LineResponse from(OrderLine l) {
            return new LineResponse(l.productId(), l.sku(), l.description(), l.quantity(), l.unitPrice(),
                    l.discountPercent(), l.taxPercent(), l.deliveredQuantity(), l.productType(), l.subtotal(),
                    l.total());
        }
    }

    public record SalesOrderResponse(
            String salesOrderId,
            String number,
            String state,
            String customerId,
            String customerName,
            String leadId,
            String currency,
            LocalDate validUntil,
            String deliveryAddress,
            String salesperson,
            String note,
            String deliveryStatus,
            String invoiceStatus,
            BigDecimal untaxedAmount,
            BigDecimal taxAmount,
            BigDecimal totalAmount,
            List<LineResponse> lines,
            Instant confirmedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static SalesOrderResponse from(SalesOrder o) {
            return new SalesOrderResponse(o.getOrderId().toString(), o.getNumber(), o.getState().name(),
                    o.getCustomerId(), o.getCustomerName(), o.getLeadId(), o.getCurrency(), o.getValidUntil(),
                    o.getDeliveryAddress(), o.getSalesperson(), o.getNote(), o.getDeliveryStatus().name(),
                    o.getInvoiceStatus().name(), o.untaxedAmount(), o.taxAmount(), o.totalAmount(),
                    o.getLines().stream().map(LineResponse::from).toList(), o.getConfirmedAt(), o.getCreatedAt(),
                    o.getUpdatedAt());
        }
    }

    public record SalesOrderPageResponse(List<SalesOrderResponse> data, int page, int pageSize, long total) {
        public static SalesOrderPageResponse from(PageResult<SalesOrder> page) {
            return new SalesOrderPageResponse(page.data().stream().map(SalesOrderResponse::from).toList(),
                    page.page(), page.pageSize(), page.total());
        }
    }

    public record StateTotalsResponse(String state, long count, BigDecimal totalAmount) {
        static StateTotalsResponse from(StateTotals t) {
            return new StateTotalsResponse(t.state().name(), t.count(), t.totalAmount());
        }
    }

    public record SalesSummaryResponse(List<StateTotalsResponse> data) {
        public static SalesSummaryResponse from(List<StateTotals> totals) {
            return new SalesSummaryResponse(totals.stream().map(StateTotalsResponse::from).toList());
        }
    }
}
