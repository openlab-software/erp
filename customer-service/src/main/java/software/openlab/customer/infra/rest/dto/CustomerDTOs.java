package software.openlab.customer.infra.rest.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import software.openlab.customer.domain.customer.Customer;
import software.openlab.customer.domain.customer.CustomerCommand;
import software.openlab.customer.domain.shared.PageResult;

public final class CustomerDTOs {

    private CustomerDTOs() {
    }

    /** {@code type} is INDIVIDUAL (CPF) or COMPANY (CNPJ). Addresses are added afterwards, under {@code /addresses}. */
    public record CreateCustomerRequest(
            @NotBlank String type,
            @NotBlank String name,
            String tradeName,
            @NotBlank String document,
            String stateRegistration,
            LocalDate birthDate,
            String email,
            String phone
    ) {
        public CustomerCommand toCommand() {
            return new CustomerCommand(name, tradeName, document, stateRegistration, birthDate, email, phone);
        }
    }

    /**
     * {@code type} is accepted only so a client that resends the whole customer body doesn't get an
     * "unknown field" 400 — it is always ignored (the type is immutable after creation).
     */
    public record UpdateCustomerRequest(
            String type,
            @NotBlank String name,
            String tradeName,
            @NotBlank String document,
            String stateRegistration,
            LocalDate birthDate,
            String email,
            String phone
    ) {
        public CustomerCommand toCommand() {
            return new CustomerCommand(name, tradeName, document, stateRegistration, birthDate, email, phone);
        }
    }

    public record ChangeStatusRequest(@NotBlank String status) {
    }

    public record CustomerResponse(
            String customerId,
            String type,
            String status,
            String name,
            String tradeName,
            String document,
            String stateRegistration,
            LocalDate birthDate,
            String email,
            String phone,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static CustomerResponse from(Customer c) {
            return new CustomerResponse(
                    c.getCustomerId().toString(),
                    c.getType().name(),
                    c.getStatus().name(),
                    c.getName(),
                    c.getTradeName(),
                    c.getDocument(),
                    c.getStateRegistration(),
                    c.getBirthDate(),
                    c.getEmail(),
                    c.getPhone(),
                    c.getCreatedAt(),
                    c.getUpdatedAt());
        }
    }

    public record CustomerPageResponse(List<CustomerResponse> data, int page, int pageSize, long total) {
        public static CustomerPageResponse from(PageResult<Customer> page) {
            List<CustomerResponse> data = page.data().stream().map(CustomerResponse::from).toList();
            return new CustomerPageResponse(data, page.page(), page.pageSize(), page.total());
        }
    }
}
