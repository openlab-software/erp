package software.openlab.catalog.domain.unitofmeasure;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UnitOfMeasure {

    private UnitOfMeasureId unitOfMeasureId;
    private String code;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;

    public static UnitOfMeasure newUnitOfMeasure(String code, String description) {
        return new UnitOfMeasure(UnitOfMeasureId.generate(), code, description, Instant.now(), null);
    }
}
