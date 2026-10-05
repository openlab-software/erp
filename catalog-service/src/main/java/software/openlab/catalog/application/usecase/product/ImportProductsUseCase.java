package software.openlab.catalog.application.usecase.product;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import software.openlab.catalog.domain.brand.BrandId;
import software.openlab.catalog.domain.category.CategoryId;
import software.openlab.catalog.domain.shared.ApiException;
import software.openlab.catalog.domain.shared.BadRequestException;
import software.openlab.catalog.domain.shared.Csv;
import software.openlab.catalog.domain.shared.Fields;
import software.openlab.catalog.domain.unitofmeasure.UnitOfMeasureId;
import software.openlab.catalog.domain.unitofmeasure.UnitOfMeasureRepository;
import software.openlab.catalog.infra.i18n.MessageResolver;

/**
 * Not itself {@code @Transactional}: each row is processed by the
 * CDI-injected {@link ImportProductRowUseCase}, which opens its own
 * transaction per call — this is what gives every row independent
 * atomicity instead of one transaction for the whole file (Requirement 14.2/14.5).
 */
@ApplicationScoped
public class ImportProductsUseCase {

    private static final String COL_DESCRIPTION = "description";
    private static final String COL_SHORT_DESCRIPTION = "short_description";
    private static final String COL_TYPE = "type";
    private static final String COL_UOM_CODE = "unit_of_measure_code";
    private static final String COL_CATEGORY_ID = "category_id";
    private static final String COL_BRAND_ID = "brand_id";
    private static final String COL_SALE_PRICE = "sale_price";
    private static final String COL_COST_PRICE = "cost_price";

    @Inject
    MessageResolver messages;

    @Inject
    UnitOfMeasureRepository unitOfMeasureRepository;

    @Inject
    ImportProductRowUseCase importProductRow;

    public ProductImportReport execute(String csvContent) {
        if (csvContent == null || csvContent.isBlank()) {
            throw new BadRequestException("error.csv.emptyOrInvalid");
        }

        List<String> lines = Csv.splitLines(csvContent);
        if (lines.isEmpty()) {
            throw new BadRequestException("error.csv.emptyOrInvalid");
        }

        List<String> header = Csv.parseLine(lines.get(0)).stream().map(h -> h.trim().toLowerCase()).toList();
        int descriptionIdx = header.indexOf(COL_DESCRIPTION);
        if (descriptionIdx < 0) {
            throw new BadRequestException("error.csv.missingDescription");
        }
        int shortDescriptionIdx = header.indexOf(COL_SHORT_DESCRIPTION);
        int typeIdx = header.indexOf(COL_TYPE);
        int uomCodeIdx = header.indexOf(COL_UOM_CODE);
        int categoryIdIdx = header.indexOf(COL_CATEGORY_ID);
        int brandIdIdx = header.indexOf(COL_BRAND_ID);
        int salePriceIdx = header.indexOf(COL_SALE_PRICE);
        int costPriceIdx = header.indexOf(COL_COST_PRICE);

        int total = 0;
        int created = 0;
        List<ProductImportReport.RowFailure> failed = new ArrayList<>();

        for (int i = 1; i < lines.size(); i++) {
            String rawLine = lines.get(i);
            if (rawLine == null || rawLine.isBlank()) {
                continue;
            }
            int lineNumber = i + 1;
            total++;

            try {
                List<String> cols = Csv.parseLine(rawLine);

                String description = field(cols, descriptionIdx);
                String shortDescription = field(cols, shortDescriptionIdx);
                String type = field(cols, typeIdx);
                String uomCode = field(cols, uomCodeIdx);
                String categoryIdRaw = field(cols, categoryIdIdx);
                String brandIdRaw = field(cols, brandIdIdx);

                UnitOfMeasureId unitOfMeasureId = resolveUnitOfMeasureId(uomCode);
                CategoryId categoryId = CategoryId.of(Fields.requireNonBlank(categoryIdRaw, "category_id"));
                BrandId brandId = (brandIdRaw == null || brandIdRaw.isBlank()) ? null : BrandId.of(brandIdRaw);
                BigDecimal salePrice = parsePrice(field(cols, salePriceIdx), "sale_price");
                BigDecimal costPrice = parsePrice(field(cols, costPriceIdx), "cost_price");

                importProductRow.execute(description, shortDescription, type, unitOfMeasureId, categoryId, brandId, salePrice, costPrice);
                created++;
            } catch (Exception e) {
                failed.add(new ProductImportReport.RowFailure(lineNumber, errorMessage(e)));
            }
        }

        return new ProductImportReport(total, created, failed);
    }

    private UnitOfMeasureId resolveUnitOfMeasureId(String code) {
        String trimmed = Fields.requireNonBlank(code, "unit_of_measure_code");
        return unitOfMeasureRepository.findByCodeIgnoreCase(trimmed)
                .map(u -> u.getUnitOfMeasureId())
                .orElseThrow(() -> new BadRequestException("error.import.uomCodeNotFound", trimmed));
    }

    private BigDecimal parsePrice(String raw, String field) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(raw.trim());
        } catch (NumberFormatException e) {
            throw new BadRequestException("error.field.invalid", field, raw);
        }
    }

    private String field(List<String> cols, int idx) {
        if (idx < 0 || idx >= cols.size()) {
            return null;
        }
        String v = cols.get(idx);
        return v == null ? null : v.trim();
    }

    private String errorMessage(Exception e) {
        if (e instanceof ApiException apiException) {
            return messages.resolve(apiException);
        }
        return e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
    }
}
