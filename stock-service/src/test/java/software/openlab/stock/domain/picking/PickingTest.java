package software.openlab.stock.domain.picking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import software.openlab.stock.domain.shared.BadRequestException;
import software.openlab.stock.domain.shared.ConflictException;
import software.openlab.stock.domain.shared.ProductId;
import software.openlab.stock.domain.stock.StockId;

class PickingTest {

    private static final ProductId PEN = ProductId.of("prod_" + software.openlab.stock.domain.shared.Ulid.generate());
    private static final ProductId INK = ProductId.of("prod_" + software.openlab.stock.domain.shared.Ulid.generate());

    private static PickingCommand command(StockId source, StockId destination, PickingLine... lines) {
        return new PickingCommand(source, destination, null, null, null, null, List.of(lines));
    }

    private static Picking delivery() {
        PickingCommand c = command(StockId.generate(), StockId.generate(), new PickingLine(PEN, "caneta", 10, 0),
                new PickingLine(INK, "tinta", 4, 0)).validated(PickingType.DELIVERY);
        return Picking.newPicking("OUT/00001", PickingType.DELIVERY, c);
    }

    @Test
    void theCommandKeepsOnlyTheStocksOfTheTypeAndMergesRepeatedProducts() {
        StockId a = StockId.generate();
        StockId b = StockId.generate();
        PickingCommand delivery = command(a, b, new PickingLine(PEN, null, 2, 0), new PickingLine(PEN, "x", 3, 0))
                .validated(PickingType.DELIVERY);

        assertEquals(a, delivery.sourceStockId());
        assertNull(delivery.destinationStockId());
        assertEquals(1, delivery.lines().size());
        assertEquals(5, delivery.lines().get(0).demand());
        assertEquals("x", delivery.lines().get(0).description());

        assertNull(command(a, b).validated(PickingType.RECEIPT).sourceStockId());
        assertThrows(BadRequestException.class, () -> command(a, a).validated(PickingType.INTERNAL));
        assertThrows(BadRequestException.class,
                () -> command(a, b, new PickingLine(PEN, null, 0, 0)).validated(PickingType.INTERNAL));
    }

    @Test
    void validatingEverythingClosesThePickingWithNoRemainder() {
        Picking picking = delivery();
        picking.confirm();

        List<PickingLine> pending = picking.complete(Map.of());

        assertEquals(PickingState.DONE, picking.getState());
        assertEquals(0, pending.size());
        assertEquals(10, picking.getLines().get(0).done());
    }

    @Test
    void aPartialValidationReturnsTheRemainderForTheBackorder() {
        Picking picking = delivery();
        picking.confirm();

        List<PickingLine> pending = picking.complete(Map.of(PEN, 6, INK, 0));

        assertEquals(2, pending.size());
        assertEquals(4, pending.get(0).demand());
        assertEquals(4, pending.get(1).demand());
        assertEquals(6, picking.getLines().get(0).done());
    }

    @Test
    void invalidDoneQuantitiesAreRejected() {
        assertThrows(BadRequestException.class, () -> delivery().complete(Map.of(PEN, 11)));
        assertThrows(BadRequestException.class, () -> delivery().complete(Map.of(PEN, -1)));
        assertThrows(BadRequestException.class,
                () -> delivery().complete(Map.of(ProductId.of("prod_" + software.openlab.stock.domain.shared.Ulid
                        .generate()), 1)));
        assertThrows(ConflictException.class, () -> delivery().complete(Map.of(PEN, 0, INK, 0)));
    }

    @Test
    void aDonePickingIsFrozenAndTheStocksMustBeSetBeforeValidating() {
        Picking picking = delivery();
        picking.complete(Map.of());
        assertThrows(ConflictException.class, picking::cancel);
        assertThrows(ConflictException.class, () -> picking.complete(Map.of()));

        Picking receipt = Picking.newPicking("IN/00001", PickingType.RECEIPT,
                command(null, null, new PickingLine(PEN, null, 1, 0)).validated(PickingType.RECEIPT));
        assertThrows(ConflictException.class, receipt::requireStocks);
    }

    @Test
    void confirmNeedsLines() {
        Picking empty = Picking.newPicking("INT/00001", PickingType.INTERNAL,
                command(StockId.generate(), StockId.generate()).validated(PickingType.INTERNAL));
        assertThrows(ConflictException.class, empty::confirm);
    }
}
