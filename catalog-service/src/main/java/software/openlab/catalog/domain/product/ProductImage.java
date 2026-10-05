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
public class ProductImage {

    private ProductImageId imageId;
    private ProductId productId;
    private String url;
    private boolean primary;
    private Instant createdAt;

    public static ProductImage newProductImage(ProductId productId, String url, boolean primary) {
        return new ProductImage(ProductImageId.generate(), productId, url, primary, Instant.now());
    }
}
