package software.openlab.accounting.infra.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import software.openlab.accounting.domain.shared.ProcessedEventRepository;

@ApplicationScoped
public class ProcessedEventRepositoryImpl implements ProcessedEventRepository {

    @Inject
    EntityManager em;

    @Override
    public boolean firstTime(String eventKey) {
        int inserted = em.createNativeQuery(
                        "insert into accounting.processed_events (event_key) values (:key) on conflict do nothing")
                .setParameter("key", eventKey)
                .executeUpdate();
        return inserted > 0;
    }
}
