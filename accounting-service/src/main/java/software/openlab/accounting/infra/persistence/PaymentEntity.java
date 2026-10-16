package software.openlab.accounting.infra.persistence;

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
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "payments", schema = "accounting")
public class PaymentEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    public Long id;

    @Column(name = "public_id", nullable = false, unique = true)
    public String publicId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    public InvoiceEntity invoice;

    @Column(name = "amount", nullable = false)
    public BigDecimal amount;

    @Column(name = "paid_on", nullable = false)
    public LocalDate paidOn;

    @Column(name = "method", nullable = false)
    public String method;

    @Column(name = "reference")
    public String reference;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;
}
