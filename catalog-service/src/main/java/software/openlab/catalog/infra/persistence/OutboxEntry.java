package software.openlab.catalog.infra.persistence;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "outbox_entries", schema = "catalog")
public class OutboxEntry extends PanacheEntityBase {

    public static final String PENDING = "pending";
    public static final String PUBLISHED = "published";
    public static final String FAILED = "failed";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "outbox_entry_id")
    public Long id;

    @Column(name = "routing_key", nullable = false)
    public String routingKey;

    @Column(name = "payload", nullable = false, columnDefinition = "text")
    public String payload;

    @Column(name = "status", nullable = false)
    public String status = PENDING;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt = Instant.now();

    @Column(name = "published_at")
    public Instant publishedAt;

    @Column(name = "error", columnDefinition = "text")
    public String error;
}
