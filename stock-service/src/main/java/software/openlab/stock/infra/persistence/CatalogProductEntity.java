package software.openlab.stock.infra.persistence;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Read-only projection of catalog.products, owned and migrated by catalog-service. Used
 * only to resolve a product's internal numeric id from its public id; never written to.
 */
@Entity
@Table(name = "products", schema = "catalog")
public class CatalogProductEntity extends PanacheEntityBase {

    @Id
    @Column(name = "product_id")
    public Long id;

    @Column(name = "public_id")
    public String publicId;
}
