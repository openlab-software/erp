package software.openlab.catalog.application.usecase.supplier;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.openlab.catalog.domain.shared.PageResult;
import software.openlab.catalog.domain.supplier.Supplier;
import software.openlab.catalog.domain.supplier.SupplierRepository;

@ApplicationScoped
public class ListSuppliersUseCase {

    @Inject
    SupplierRepository repository;

    public PageResult<Supplier> execute(String q, Integer page, Integer pageSize) {
        int normalizedPage = PageResult.normalizePage(page);
        int normalizedPageSize = PageResult.normalizePageSize(pageSize);
        return repository.find(q, normalizedPage, normalizedPageSize);
    }
}
