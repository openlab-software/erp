package software.openlab.stock.infra.persistence;

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
import java.time.Instant;

/** StockCount is append-only: rows are inserted by CreateStockCountUseCase and never updated. */
@Entity
@Table(name = "stock_counts", schema = "stock")
public class StockCountEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_count_id")
    public Long id;

    @Column(name = "public_id", nullable = false, unique = true)
    public String publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    public StockEntity stock;

    @Column(name = "catalog_product_public_id", nullable = false)
    public String catalogProductPublicId;

    @Column(name = "system_value", nullable = false)
    public int systemValue;

    @Column(name = "counted_value", nullable = false)
    public int countedValue;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;
}
