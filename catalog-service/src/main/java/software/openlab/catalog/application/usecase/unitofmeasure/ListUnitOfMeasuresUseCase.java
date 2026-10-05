package software.openlab.catalog.application.usecase.unitofmeasure;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.openlab.catalog.domain.shared.PageResult;
import software.openlab.catalog.domain.unitofmeasure.UnitOfMeasure;
import software.openlab.catalog.domain.unitofmeasure.UnitOfMeasureRepository;

@ApplicationScoped
public class ListUnitOfMeasuresUseCase {

    @Inject
    UnitOfMeasureRepository repository;

    public PageResult<UnitOfMeasure> execute(String q, Integer page, Integer pageSize) {
        int normalizedPage = PageResult.normalizePage(page);
        int normalizedPageSize = PageResult.normalizePageSize(pageSize);
        return repository.find(q, normalizedPage, normalizedPageSize);
    }
}
