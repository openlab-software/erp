package software.openlab.customer.infra.messaging;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import org.jboss.logging.Logger;
import software.openlab.customer.infra.persistence.OutboxEntry;

/**
 * Polls {@code customer.outbox_entries} for pending events and publishes them to
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

    @Scheduled(every = "${customer.outbox.relay-interval:5s}")
    @Transactional
    void flush() {
        List<OutboxEntry> pending = OutboxEntry
                .find("status", Sort.by("createdAt"), OutboxEntry.PENDING)
                .page(Page.ofSize(BATCH_SIZE))
                .list();

        for (OutboxEntry entry : pending) {
            publishEntry(entry);
        }
    }

    private void publishEntry(OutboxEntry entry) {
        try {
            rabbitMqPublisher.publish(entry.routingKey, entry.payload);
            entry.status = OutboxEntry.PUBLISHED;
            entry.publishedAt = Instant.now();
        } catch (Exception e) {
            entry.status = OutboxEntry.FAILED;
            entry.error = e.getMessage();
            LOG.errorf(e, "[Outbox] publish failed — id=%d routingKey=%s", entry.id, entry.routingKey);
        }
    }
}
