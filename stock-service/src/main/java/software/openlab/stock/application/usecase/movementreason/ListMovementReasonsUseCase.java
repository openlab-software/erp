package software.openlab.stock.application.usecase.movementreason;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.openlab.stock.domain.movementreason.MovementReason;
import software.openlab.stock.domain.movementreason.MovementReasonRepository;
import software.openlab.stock.domain.shared.BadRequestException;
import software.openlab.stock.domain.shared.PageResult;

@ApplicationScoped
public class ListMovementReasonsUseCase {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    @Inject
    MovementReasonRepository movementReasonRepository;

    public PageResult<MovementReason> execute(String q, int page, int pageSize) {
        int safePage = page < 1 ? DEFAULT_PAGE : page;
        int safePageSize = pageSize <= 0 ? DEFAULT_PAGE_SIZE : pageSize;
        if (safePageSize > MAX_PAGE_SIZE) {
            throw new BadRequestException("error.pageSize.max", MAX_PAGE_SIZE);
        }
        return movementReasonRepository.findAll(q, safePage, safePageSize);
    }
}
