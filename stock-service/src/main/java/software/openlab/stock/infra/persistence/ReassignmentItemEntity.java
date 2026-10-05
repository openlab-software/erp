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
@Table(name = "reassignment_items", schema = "stock")
public class ReassignmentItemEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reassignment_item_id")
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reassignment_id", nullable = false)
    public ReassignmentEntity reassignment;

    @Column(name = "catalog_product_id")
    public Long catalogProductId;

    @Column(name = "catalog_product_public_id", nullable = false)
    public String catalogProductPublicId;

    @Column(nullable = false)
    public int quantity;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;
}
