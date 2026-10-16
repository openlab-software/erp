package software.openlab.sales.infra.persistence;

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
@Table(name = "sales_orders", schema = "sales")
public class SalesOrderEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sales_order_id")
    public Long id;

    @Column(name = "public_id", nullable = false, unique = true)
    public String publicId;

    @Column(name = "number", nullable = false, unique = true)
    public String number;

    @Column(name = "state", nullable = false)
    public String state;

    @Column(name = "customer_id", nullable = false)
    public String customerId;

    @Column(name = "customer_name", nullable = false)
    public String customerName;

    @Column(name = "lead_id")
    public String leadId;

    @Column(name = "currency", nullable = false)
    public String currency;

    @Column(name = "valid_until")
    public LocalDate validUntil;

    @Column(name = "delivery_address", columnDefinition = "text")
    public String deliveryAddress;

    @Column(name = "salesperson")
    public String salesperson;

    @Column(name = "note", columnDefinition = "text")
    public String note;

    @Column(name = "delivery_status", nullable = false)
    public String deliveryStatus;

    @Column(name = "invoice_status", nullable = false)
    public String invoiceStatus;

    @Column(name = "confirmed_at")
    public Instant confirmedAt;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "updated_at")
    public Instant updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("position ASC")
    public List<SalesOrderLineEntity> lines = new ArrayList<>();
}
