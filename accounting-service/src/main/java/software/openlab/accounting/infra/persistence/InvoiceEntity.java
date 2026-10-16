package software.openlab.accounting.infra.persistence;

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
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name = "invoices", schema = "accounting")
public class InvoiceEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_id")
    public Long id;

    @Column(name = "public_id", nullable = false, unique = true)
    public String publicId;

    @Column(name = "kind", nullable = false)
    public String kind;

    @Column(name = "number", unique = true)
    public String number;

    @Column(name = "state", nullable = false)
    public String state;

    @Column(name = "payment_state", nullable = false)
    public String paymentState;

    @Column(name = "partner_id", nullable = false)
    public String partnerId;

    @Column(name = "partner_name", nullable = false)
    public String partnerName;

    @Column(name = "currency", nullable = false)
    public String currency;

    @Column(name = "invoice_date")
    public LocalDate invoiceDate;

    @Column(name = "due_date")
    public LocalDate dueDate;

    @Column(name = "reference")
    public String reference;

    @Column(name = "note", columnDefinition = "text")
    public String note;

    @Column(name = "origin_type")
    public String originType;

    @Column(name = "origin_id")
    public String originId;

    @Column(name = "origin_number")
    public String originNumber;

    @Column(name = "untaxed_amount", nullable = false)
    public BigDecimal untaxedAmount;

    @Column(name = "tax_amount", nullable = false)
    public BigDecimal taxAmount;

    @Column(name = "total_amount", nullable = false)
    public BigDecimal totalAmount;

    @Column(name = "paid_amount", nullable = false)
    public BigDecimal paidAmount;

    @Column(name = "posted_at")
    public Instant postedAt;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "updated_at")
    public Instant updatedAt;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @OrderBy("position ASC")
    public List<InvoiceLineEntity> lines = new ArrayList<>();

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @OrderBy("paidOn ASC, id ASC")
    public List<PaymentEntity> payments = new ArrayList<>();
}
