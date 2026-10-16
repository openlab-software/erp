package software.openlab.stock.infra.persistence;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pickings", schema = "stock")
public class PickingEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "picking_id")
    public Long id;

    @Column(name = "public_id", nullable = false, unique = true)
    public String publicId;

    @Column(name = "number", nullable = false, unique = true)
    public String number;

    @Column(name = "type", nullable = false)
    public String type;

    @Column(name = "state", nullable = false)
    public String state;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "source_stock_id")
    public StockEntity sourceStock;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "destination_stock_id")
    public StockEntity destinationStock;

    @Column(name = "partner_id")
    public String partnerId;

    @Column(name = "partner_name")
    public String partnerName;

    @Column(name = "origin_type")
    public String originType;

    @Column(name = "origin_id")
    public String originId;

    @Column(name = "origin_number")
    public String originNumber;

    @Column(name = "backorder_of")
    public String backorderOf;

    @Column(name = "scheduled_date")
    public LocalDate scheduledDate;

    @Column(name = "note", columnDefinition = "text")
    public String note;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "updated_at")
    public Instant updatedAt;

    @Column(name = "done_at")
    public Instant doneAt;

    @OneToMany(mappedBy = "picking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("position ASC")
    public List<PickingLineEntity> lines = new ArrayList<>();
}
