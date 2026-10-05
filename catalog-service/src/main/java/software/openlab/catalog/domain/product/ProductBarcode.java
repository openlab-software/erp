package software.openlab.catalog.domain.product;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductBarcode {

    private ProductBarcodeId barcodeId;
    private ProductId productId;
    private String code;
    private BarcodeType type;
    private Instant createdAt;

    public static ProductBarcode newProductBarcode(ProductId productId, String code, BarcodeType type) {
        return new ProductBarcode(ProductBarcodeId.generate(), productId, code, type, Instant.now());
    }
}
