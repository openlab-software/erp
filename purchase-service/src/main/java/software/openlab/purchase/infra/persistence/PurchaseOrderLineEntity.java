package software.openlab.purchase.infra.persistence;

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
@Table(name = "purchase_order_lines", schema = "purchase")
public class PurchaseOrderLineEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchase_order_line_id")
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    public PurchaseOrderEntity order;

    @Column(name = "position", nullable = false)
    public int position;

    @Column(name = "product_id", nullable = false)
    public String productId;

    @Column(name = "sku")
    public String sku;

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

    @Column(name = "received_quantity", nullable = false)
    public BigDecimal receivedQuantity;

    @Column(name = "product_type", nullable = false)
    public String productType;
}
