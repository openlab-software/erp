package software.openlab.crm.infra.persistence;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "leads", schema = "crm")
public class LeadEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lead_id")
    public Long id;

    @Column(name = "public_id", nullable = false, unique = true)
    public String publicId;

    @Column(name = "name", nullable = false)
    public String name;

    @Column(name = "type", nullable = false)
    public String type;

    @Column(name = "status", nullable = false)
    public String status;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "stage_id", nullable = false)
    public StageEntity stage;

    @Column(name = "customer_id")
    public String customerId;

    @Column(name = "contact_name")
    public String contactName;

    @Column(name = "company_name")
    public String companyName;

    @Column(name = "email")
    public String email;

    @Column(name = "phone")
    public String phone;

    @Column(name = "expected_revenue", nullable = false)
    public BigDecimal expectedRevenue;

    @Column(name = "probability", nullable = false)
    public int probability;

    @Column(name = "priority", nullable = false)
    public int priority;

    @Column(name = "expected_closing")
    public LocalDate expectedClosing;

    @Column(name = "description", columnDefinition = "text")
    public String description;

    @Column(name = "assigned_to")
    public String assignedTo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lost_reason_id")
    public LostReasonEntity lostReason;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "lead_tags", schema = "crm", joinColumns = @JoinColumn(name = "lead_id"))
    @Column(name = "tag")
    public Set<String> tags = new LinkedHashSet<>();

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "updated_at")
    public Instant updatedAt;

    @Column(name = "closed_at")
    public Instant closedAt;
}
