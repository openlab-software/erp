package software.openlab.purchase.infra.persistence;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_orders", schema = "purchase")
public class PurchaseOrderEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchase_order_id")
    public Long id;

    @Column(name = "public_id", nullable = false, unique = true)
    public String publicId;

    @Column(name = "number", nullable = false, unique = true)
    public String number;

    @Column(name = "state", nullable = false)
    public String state;

    @Column(name = "supplier_id", nullable = false)
    public String supplierId;

    @Column(name = "supplier_name", nullable = false)
    public String supplierName;

    @Column(name = "origin")
    public String origin;

    @Column(name = "currency", nullable = false)
    public String currency;

    @Column(name = "expected_date")
    public LocalDate expectedDate;

    @Column(name = "ship_to_address", columnDefinition = "text")
    public String shipToAddress;

    @Column(name = "buyer")
    public String buyer;

    @Column(name = "note", columnDefinition = "text")
    public String note;

    @Column(name = "receipt_status", nullable = false)
    public String receiptStatus;

    @Column(name = "bill_status", nullable = false)
    public String billStatus;

    @Column(name = "confirmed_at")
    public Instant confirmedAt;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "updated_at")
    public Instant updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("position ASC")
    public List<PurchaseOrderLineEntity> lines = new ArrayList<>();
}
