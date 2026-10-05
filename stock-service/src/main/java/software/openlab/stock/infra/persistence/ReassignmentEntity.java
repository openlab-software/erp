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

@Entity
@Table(name = "reassignments", schema = "stock")
public class ReassignmentEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reassignment_id")
    public Long id;

    @Column(name = "public_id", nullable = false, unique = true)
    public String publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_stock_id", nullable = false)
    public StockEntity fromStock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_stock_id", nullable = false)
    public StockEntity toStock;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;
}
