package software.openlab.stock.domain.reassignment;

public interface ReassignmentRepository {

    /**
     * Resolves from/to stocks and each item's catalog product, then persists the
     * reassignment and its items. Throws {@link software.openlab.stock.domain.shared.NotFoundException}
     * if from/to stock is missing, or {@link software.openlab.stock.domain.shared.BadRequestException}
     * if an item's product is not found in the catalog projection.
     */
    void save(Reassignment reassignment);
}
