package software.openlab.crm.infra.persistence;

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
import java.time.LocalDate;

@Entity
@Table(name = "activities", schema = "crm")
public class ActivityEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_id")
    public Long id;

    @Column(name = "public_id", nullable = false, unique = true)
    public String publicId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "lead_id", nullable = false)
    public LeadEntity lead;

    @Column(name = "type", nullable = false)
    public String type;

    @Column(name = "summary", nullable = false)
    public String summary;

    @Column(name = "note", columnDefinition = "text")
    public String note;

    @Column(name = "due_date")
    public LocalDate dueDate;

    @Column(name = "assigned_to")
    public String assignedTo;

    @Column(name = "done", nullable = false)
    public boolean done;

    @Column(name = "done_at")
    public Instant doneAt;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;
}
