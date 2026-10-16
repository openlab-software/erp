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
@Table(name = "customer_contacts", schema = "customer")
public class ContactEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_contact_id")
    public Long id;

    @Column(name = "public_id", nullable = false, unique = true)
    public String publicId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    public CustomerEntity customer;

    /** Creation order within the customer. */
    @Column(name = "position", nullable = false)
    public int position;

    @Column(name = "name", nullable = false)
    public String name;

    @Column(name = "job_title")
    public String jobTitle;

    @Column(name = "email")
    public String email;

    @Column(name = "phone")
    public String phone;

    @Column(name = "notes", columnDefinition = "text")
    public String notes;

    @Column(name = "is_primary", nullable = false)
    public boolean isPrimary;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt = Instant.now();
}
