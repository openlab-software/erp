package software.openlab.customer.infra.persistence;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "customers", schema = "customer")
public class CustomerEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    public Long id;

    @Column(name = "public_id", nullable = false, unique = true)
    public String publicId;

    @Column(name = "type", nullable = false)
    public String type;

    @Column(name = "status", nullable = false)
    public String status;

    @Column(name = "name", nullable = false)
    public String name;

    @Column(name = "trade_name")
    public String tradeName;

    @Column(name = "document", nullable = false, unique = true)
    public String document;

    @Column(name = "state_registration")
    public String stateRegistration;

    @Column(name = "birth_date")
    public LocalDate birthDate;

    @Column(name = "email")
    public String email;

    @Column(name = "phone")
    public String phone;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "updated_at")
    public Instant updatedAt;
}
