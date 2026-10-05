package software.openlab.catalog.domain.brand;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Brand {

    private BrandId brandId;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;

    public static Brand newBrand(String description) {
        return new Brand(BrandId.generate(), description, Instant.now(), null);
    }
}
