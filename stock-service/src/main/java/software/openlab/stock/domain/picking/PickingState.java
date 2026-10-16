package software.openlab.stock.domain.picking;

import software.openlab.stock.domain.shared.BadRequestException;

public enum PickingState {
    DRAFT,
    CONFIRMED,
    DONE,
    CANCELLED;

    public static PickingState parse(String raw) {
        try {
            return valueOf(raw.trim().toUpperCase());
        } catch (RuntimeException e) {
            throw new BadRequestException("error.picking.stateInvalid");
        }
    }
}
