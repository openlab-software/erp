package software.openlab.crm.infra.messaging;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import org.jboss.logging.Logger;
import software.openlab.crm.infra.persistence.OutboxEntry;

/**
 * Polls {@code crm.outbox_entries} for pending events and publishes them to
 * RabbitMQ, marking each as published/failed. Runs inside the same Quarkus
 * process as a simplification over the a separate relay binary —
 * acceptable per the task's architecture decisions.
 */
@ApplicationScoped
public class OutboxRelayJob {

    private static final Logger LOG = Logger.getLogger(OutboxRelayJob.class);
    private static final int BATCH_SIZE = 100;

    @Inject
    RabbitMqPublisher rabbitMqPublisher;

    @Scheduled(every = "${crm.outbox.relay-interval:5s}")
    @Transactional
    void flush() {
        List<OutboxEntry> pending = OutboxEntry
                .find("status", Sort.by("createdAt"), OutboxEntry.PENDING)
                .page(Page.ofSize(BATCH_SIZE))
                .list();

        for (OutboxEntry entry : pending) {
            if (!publishEntry(entry)) {
                break; // broker unavailable: retry on the next tick, in order
            }
        }
    }

    /** @return false when the broker did not take the message; the entry stays PENDING for the next tick. */
    private boolean publishEntry(OutboxEntry entry) {
        try {
            rabbitMqPublisher.publish(entry.routingKey, entry.payload);
            entry.status = OutboxEntry.PUBLISHED;
            entry.publishedAt = Instant.now();
            entry.error = null;
            return true;
        } catch (Exception e) {
            entry.error = e.getMessage();
            LOG.errorf(e, "[Outbox] publish failed — id=%d routingKey=%s", entry.id, entry.routingKey);
            return false;
        }
    }
}
