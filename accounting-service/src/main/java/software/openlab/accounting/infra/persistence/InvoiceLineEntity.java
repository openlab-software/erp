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

@Entity
@Table(name = "invoice_lines", schema = "accounting")
public class InvoiceLineEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_line_id")
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    public InvoiceEntity invoice;

    @Column(name = "position", nullable = false)
    public int position;

    @Column(name = "product_id")
    public String productId;

    @Column(name = "description", nullable = false)
    public String description;

    @Column(name = "quantity", nullable = false)
    public BigDecimal quantity;

    @Column(name = "unit_price", nullable = false)
    public BigDecimal unitPrice;

    @Column(name = "discount_percent", nullable = false)
    public BigDecimal discountPercent;

    @Column(name = "tax_percent", nullable = false)
    public BigDecimal taxPercent;
}
