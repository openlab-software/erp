package software.openlab.catalog.application.usecase.product;

import java.util.List;

public record ProductImportReport(int total, int created, List<RowFailure> failed) {

    public record RowFailure(int line, String error) {
    }
}
