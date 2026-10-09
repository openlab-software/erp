package software.openlab.customer.infra.persistence;

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
@Table(name = "customer_addresses", schema = "customer")
public class AddressEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_address_id")
    public Long id;

    @Column(name = "public_id", nullable = false, unique = true)
    public String publicId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    public CustomerEntity customer;

    /** Creation order within the customer. */
    @Column(name = "position", nullable = false)
    public int position;

    @Column(name = "label")
    public String label;

    @Column(name = "zip_code")
    public String zipCode;

    @Column(name = "street")
    public String street;

    @Column(name = "street_number")
    public String streetNumber;

    @Column(name = "complement")
    public String complement;

    @Column(name = "neighborhood")
    public String neighborhood;

    @Column(name = "city")
    public String city;

    @Column(name = "state")
    public String state;

    @Column(name = "is_default", nullable = false)
    public boolean isDefault;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt = Instant.now();
}
