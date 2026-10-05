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

/**
 * StockItem is reactive-only: it is created by the product.created handler, updated by
 * product.updated/product.deleted handlers, and never exposed through a dedicated CRUD API
 * (out of scope per the spec's introduction).
 */
@Entity
@Table(name = "items", schema = "stock")
public class StockItemEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_item_id")
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    public StockEntity stock;

    /**
     * Internal numeric id captured from catalog.products at item-creation time. May become
     * stale after the source row is hard-deleted — {@link #catalogProductPublicId} is the
     * reliable key used to match incoming catalog.events.
     */
    @Column(name = "catalog_product_id")
    public Long catalogProductId;

    @Column(name = "catalog_product_public_id", nullable = false)
    public String catalogProductPublicId;

    @Column(name = "min_value")
    public Integer minValue;

    @Column(name = "current_value", nullable = false)
    public int currentValue;

    @Column(name = "max_value")
    public Integer maxValue;

    @Column(name = "reserved_value", nullable = false)
    public int reservedValue;

    @Column(nullable = false)
    public boolean active = true;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "modified_at")
    public Instant modifiedAt;
}
