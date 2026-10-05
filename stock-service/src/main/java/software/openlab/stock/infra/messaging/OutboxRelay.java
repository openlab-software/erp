package software.openlab.stock.infra.messaging;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import org.jboss.logging.Logger;
import software.openlab.stock.infra.persistence.OutboxEntryEntity;

/**
 * Equivalent of go-common/outbox.Relay, running as a Quarkus @Scheduled job in the same
 * process instead of a separate cmd/relay binary. Polls stock.outbox_entries for pending
 * rows and publishes them to RabbitMQ, marking each as published/failed.
 */
@ApplicationScoped
public class OutboxRelay {

    private static final Logger LOG = Logger.getLogger(OutboxRelay.class);
    private static final int BATCH_SIZE = 100;

    @Inject
    RabbitMqGateway rabbitMqGateway;

    @Scheduled(every = "{stock.outbox.relay.interval}")
    @Transactional
    void flush() {
        List<OutboxEntryEntity> pending = OutboxEntryEntity
                .find("status", Sort.ascending("createdAt"), OutboxEntryEntity.PENDING)
                .page(Page.ofSize(BATCH_SIZE))
                .list();

        for (OutboxEntryEntity entry : pending) {
            publishEntry(entry);
        }
    }

    private void publishEntry(OutboxEntryEntity entry) {
        try {
            rabbitMqGateway.publish(entry.routingKey, entry.payload.getBytes(StandardCharsets.UTF_8));
            entry.status = OutboxEntryEntity.PUBLISHED;
            entry.publishedAt = Instant.now();
        } catch (Exception e) {
            LOG.errorf(e, "failed to publish outbox entry id=%d routing_key=%s", entry.id, entry.routingKey);
            entry.status = OutboxEntryEntity.FAILED;
            entry.error = e.getMessage();
        }
    }
}
