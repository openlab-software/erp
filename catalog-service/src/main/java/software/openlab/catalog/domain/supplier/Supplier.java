package software.openlab.catalog.domain.supplier;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Supplier {

    private SupplierId supplierId;
    private String name;
    private String document;
    private Instant createdAt;
    private Instant updatedAt;

    public static Supplier newSupplier(String name, String document) {
        return new Supplier(SupplierId.generate(), name, document, Instant.now(), null);
    }
}
