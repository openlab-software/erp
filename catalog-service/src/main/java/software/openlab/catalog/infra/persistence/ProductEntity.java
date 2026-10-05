package software.openlab.catalog.infra.persistence;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "products", schema = "catalog")
public class ProductEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    public Long id;

    @Column(name = "public_id", nullable = false, unique = true)
    public String publicId;

    @Column(name = "description", nullable = false)
    public String description;

    @Column(name = "short_description", nullable = false)
    public String shortDescription;

    @Column(name = "type", nullable = false)
    public String type;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unit_of_measure_id", nullable = false)
    public UnitOfMeasureEntity unitOfMeasure;

    @Column(name = "status", nullable = false)
    public String status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    public CategoryEntity category;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "brand_id", nullable = true)
    public BrandEntity brand;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "default_supplier_id", nullable = true)
    public SupplierEntity defaultSupplier;

    @Column(name = "sale_price", nullable = false)
    public BigDecimal salePrice;

    @Column(name = "cost_price", nullable = false)
    public BigDecimal costPrice;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "updated_at")
    public Instant updatedAt;
}
