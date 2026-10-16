package software.openlab.stock.infra.rest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import software.openlab.stock.application.usecase.picking.ListReplenishmentUseCase;
import software.openlab.stock.application.usecase.picking.ValidatePickingUseCase;
import software.openlab.stock.domain.picking.Picking;
import software.openlab.stock.domain.picking.PickingCommand;
import software.openlab.stock.domain.picking.PickingLine;
import software.openlab.stock.domain.shared.PageResult;
import software.openlab.stock.domain.shared.ProductId;
import software.openlab.stock.domain.stock.StockId;
import software.openlab.stock.domain.stockitem.StockItem;

public final class PickingDTOs {

    private PickingDTOs() {
    }

    public record LineRequest(@NotBlank String productId, String description, @Min(1) int demand) {
        PickingLine toDomain() {
            return new PickingLine(ProductId.of(productId), description, demand, 0);
        }
    }

    /** {@code type} (RECEIPT, DELIVERY or INTERNAL) is read only on creation. */
    public record PickingRequest(
            String type,
            String sourceStockId,
            String destinationStockId,
            String partnerId,
            String partnerName,
            LocalDate scheduledDate,
            String note,
            @Valid @Size(max = PickingCommand.MAX_LINES) List<LineRequest> lines
    ) {
        public PickingCommand toCommand() {
            return new PickingCommand(
                    sourceStockId == null || sourceStockId.isBlank() ? null : StockId.of(sourceStockId),
                    destinationStockId == null || destinationStockId.isBlank() ? null
                            : StockId.of(destinationStockId),
                    partnerId, partnerName, scheduledDate, note,
                    lines == null ? List.of() : lines.stream().map(LineRequest::toDomain).toList());
        }
    }

    public record DoneRequest(@NotBlank String productId, @Min(0) int quantity) {
    }

    /**
     * {@code done} lists the quantities actually moved; a product that is absent moves its whole demand.
     * {@code createBackorder} (default true) turns the unmoved remainder into a new picking.
     */
    public record ValidateRequest(@Valid List<DoneRequest> done, Boolean createBackorder) {
        public Map<ProductId, Integer> doneByProduct() {
            Map<ProductId, Integer> map = new HashMap<>();
            if (done != null) {
                done.forEach(d -> map.put(ProductId.of(d.productId()), d.quantity()));
            }
            return map;
        }

        public boolean backorder() {
            return createBackorder == null || createBackorder;
        }
    }

    public record LineResponse(String productId, String description, int demand, int done) {
        static LineResponse from(PickingLine l) {
            return new LineResponse(l.productId().toString(), l.description(), l.demand(), l.done());
        }
    }

    public record PickingResponse(
            String pickingId,
            String number,
            String type,
            String state,
            String sourceStockId,
            String destinationStockId,
            String partnerId,
            String partnerName,
            String originType,
            String originId,
            String originNumber,
            String backorderOf,
            LocalDate scheduledDate,
            String note,
            List<LineResponse> lines,
            Instant createdAt,
            Instant updatedAt,
            Instant doneAt
    ) {
        public static PickingResponse from(Picking p) {
            return new PickingResponse(p.getPickingId().toString(), p.getNumber(), p.getType().name(),
                    p.getState().name(),
                    p.getSourceStockId() == null ? null : p.getSourceStockId().toString(),
                    p.getDestinationStockId() == null ? null : p.getDestinationStockId().toString(),
                    p.getPartnerId(), p.getPartnerName(), p.getOriginType(), p.getOriginId(), p.getOriginNumber(),
                    p.getBackorderOf() == null ? null : p.getBackorderOf().toString(), p.getScheduledDate(),
                    p.getNote(), p.getLines().stream().map(LineResponse::from).toList(), p.getCreatedAt(),
                    p.getUpdatedAt(), p.getDoneAt());
        }
    }

    public record ValidateResponse(PickingResponse picking, PickingResponse backorder) {
        public static ValidateResponse from(ValidatePickingUseCase.Result result) {
            return new ValidateResponse(PickingResponse.from(result.picking()),
                    result.backorder() == null ? null : PickingResponse.from(result.backorder()));
        }
    }

    public record ReplenishmentResponse(String stockId, String productId, int currentValue, int reservedValue,
                                        Integer minValue, Integer maxValue, int suggestedQuantity) {
        static ReplenishmentResponse from(StockItem i) {
            return new ReplenishmentResponse(i.stockId().toString(), i.productId().toString(), i.currentValue(),
                    i.reservedValue(), i.minValue(), i.maxValue(), ListReplenishmentUseCase.suggestedQuantity(i));
        }
    }

    public static ListingPageResponse<PickingResponse> page(PageResult<Picking> page) {
        return new ListingPageResponse<>(page.data().stream().map(PickingResponse::from).toList(), page.page(),
                page.pageSize(), page.total());
    }

    public static ListingPageResponse<ReplenishmentResponse> replenishmentPage(PageResult<StockItem> page) {
        return new ListingPageResponse<>(page.data().stream().map(ReplenishmentResponse::from).toList(),
                page.page(), page.pageSize(), page.total());
    }
}
