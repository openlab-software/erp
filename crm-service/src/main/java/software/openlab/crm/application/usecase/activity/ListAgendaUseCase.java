package software.openlab.crm.application.usecase.activity;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDate;
import software.openlab.crm.domain.activity.Activity;
import software.openlab.crm.domain.activity.ActivityRepository;
import software.openlab.crm.domain.shared.PageResult;

/** Activities across all leads: the "my activities" screen (pending ones due up to a date, per assignee). */
@ApplicationScoped
public class ListAgendaUseCase {

    @Inject
    ActivityRepository repository;

    public PageResult<Activity> execute(Boolean done, LocalDate dueBefore, String assignedTo, Integer page,
                                        Integer pageSize) {
        return repository.find(done, dueBefore, assignedTo, PageResult.normalizePage(page),
                PageResult.normalizePageSize(pageSize));
    }
}
