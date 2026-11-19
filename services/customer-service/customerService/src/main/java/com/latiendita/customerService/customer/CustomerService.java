package com.latiendita.customerService.customer;

import com.latiendita.customerService.CurrentUser;
import com.latiendita.customerService.customer.dto.CreateCustomerRequest;
import com.latiendita.customerService.customer.dto.CustomerDto;
import com.latiendita.customerService.customer.dto.CustomerMapper;
import com.latiendita.customerService.customer.dto.CustomerSummaryDto;
import com.latiendita.customerService.customer.dto.NewsletterDto;
import com.latiendita.customerService.customer.dto.NewsletterRequest;
import com.latiendita.customerService.customer.dto.UpdateCustomerRequest;
import com.latiendita.customerService.customer.event.CustomerCreatedEvent;
import com.latiendita.customerService.exception.CustomerNotFoundException;
import com.latiendita.customerService.exception.DuplicateEmailException;
import com.latiendita.customerService.outbox.OutboxWriter;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {

    public static final String CUSTOMER_EVENTS_TOPIC = "customer-events";
    public static final String CUSTOMER_CREATED = "CUSTOMER_CREATED";

    private final CustomerRepository customerRepository;
    private final OutboxWriter outboxWriter;

    /**
     * Idempotent first-login profile creation keyed by the trusted {@code sub}
     * from X-User-Id. The client-supplied {@code authorizerSub} is ignored.
     */
    public CustomerCreation create(CreateCustomerRequest request) {
        String sub = CurrentUser.id();
        String email = resolveEmail(request.email());

        Optional<CustomerEntity> existing = customerRepository.findByAuthorizerSubAndDeletedAtIsNull(sub);
        if (existing.isPresent()) {
            return new CustomerCreation(CustomerMapper.toDto(existing.get()), false);
        }
        if (customerRepository.existsByEmailAndDeletedAtIsNull(email)) {
            throw new DuplicateEmailException(email);
        }

        Instant now = Instant.now();
        CustomerEntity customer = CustomerEntity.builder()
                .authorizerSub(sub)
                .email(email)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phone(request.phone())
                .status(CustomerStatus.ACTIVE)
                .newsletterOptIn(false)
                .lastLoginAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();

        CustomerEntity saved = customerRepository.save(customer);
        outboxWriter.append(CUSTOMER_EVENTS_TOPIC, saved.getId().toString(), CUSTOMER_CREATED,
                new CustomerCreatedEvent(CUSTOMER_CREATED, saved.getId(), sub, email, now));

        return new CustomerCreation(CustomerMapper.toDto(saved), true);
    }

    @Transactional(readOnly = true)
    public CustomerDto findMe() {
        return CustomerMapper.toDto(requireBySub(CurrentUser.id()));
    }

    public CustomerDto updateMe(UpdateCustomerRequest request) {
        CustomerEntity customer = requireBySub(CurrentUser.id());
        customer.setFirstName(request.firstName());
        customer.setLastName(request.lastName());
        customer.setPhone(request.phone());
        customer.setLastLoginAt(Instant.now());
        customer.setUpdatedAt(Instant.now());
        return CustomerMapper.toDto(customerRepository.save(customer));
    }

    @Transactional(readOnly = true)
    public Page<CustomerSummaryDto> search(String name, String email, CustomerStatus status, Pageable pageable) {
        Specification<CustomerEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (hasText(name)) {
                String pattern = "%" + name.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("firstName")), pattern),
                        cb.like(cb.lower(root.get("lastName")), pattern)));
            }
            if (hasText(email)) {
                predicates.add(cb.like(cb.lower(root.get("email")),
                        "%" + email.trim().toLowerCase() + "%"));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        return customerRepository.findAll(spec, pageable).map(CustomerMapper::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public CustomerDto findById(UUID id) {
        return customerRepository.findByIdAndDeletedAtIsNull(id)
                .map(CustomerMapper::toDto)
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    /**
     * Opting in is the explicit consent action and stamps {@code newsletterConsentAt};
     * opting out clears it immediately. The logical invariant
     * "opt-in implies consent timestamp" is preserved on both paths.
     */
    public NewsletterDto updateNewsletter(NewsletterRequest request) {
        CustomerEntity customer = requireBySub(CurrentUser.id());
        Instant now = Instant.now();
        if (request.newsletterOptIn()) {
            customer.setNewsletterOptIn(true);
            customer.setNewsletterConsentAt(now);
        } else {
            customer.setNewsletterOptIn(false);
            customer.setNewsletterConsentAt(null);
        }
        customer.setUpdatedAt(now);
        customerRepository.save(customer);
        return CustomerMapper.toNewsletterDto(customer);
    }

    private CustomerEntity requireBySub(String sub) {
        return customerRepository.findByAuthorizerSubAndDeletedAtIsNull(sub)
                .orElseThrow(() -> new CustomerNotFoundException(sub));
    }

    private String resolveEmail(String requestEmail) {
        String headerEmail = CurrentUser.email();
        return headerEmail != null ? headerEmail : requestEmail;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
