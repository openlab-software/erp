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

@Entity
@Table(name = "picking_lines", schema = "stock")
public class PickingLineEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "picking_line_id")
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "picking_id", nullable = false)
    public PickingEntity picking;

    @Column(name = "position", nullable = false)
    public int position;

    @Column(name = "catalog_product_public_id", nullable = false)
    public String catalogProductPublicId;

    @Column(name = "description")
    public String description;

    @Column(name = "demand", nullable = false)
    public int demand;

    @Column(name = "done", nullable = false)
    public int done;
}
