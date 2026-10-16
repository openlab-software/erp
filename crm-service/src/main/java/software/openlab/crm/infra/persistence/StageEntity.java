package software.openlab.crm.infra.persistence;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "stages", schema = "crm")
public class StageEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stage_id")
    public Long id;

    @Column(name = "public_id", nullable = false, unique = true)
    public String publicId;

    @Column(name = "name", nullable = false)
    public String name;

    @Column(name = "position", nullable = false)
    public int position;

    @Column(name = "probability", nullable = false)
    public int probability;

    @Column(name = "is_won", nullable = false)
    public boolean won;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "updated_at")
    public Instant updatedAt;
}
