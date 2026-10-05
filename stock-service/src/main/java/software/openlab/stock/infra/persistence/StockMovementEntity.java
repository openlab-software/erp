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

/** StockMovement is append-only: rows are inserted by CreateStockMovementUseCase and never updated. */
@Entity
@Table(name = "movements", schema = "stock")
public class StockMovementEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_movement_id")
    public Long id;

    @Column(name = "public_id", nullable = false, unique = true)
    public String publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    public StockEntity stock;

    @Column(name = "catalog_product_public_id", nullable = false)
    public String catalogProductPublicId;

    @Column(nullable = false)
    public String type;

    @Column(nullable = false)
    public int quantity;

    @Column(name = "resulting_balance", nullable = false)
    public int resultingBalance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reason_id")
    public MovementReasonEntity reason;

    @Column
    public String reference;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;
}
