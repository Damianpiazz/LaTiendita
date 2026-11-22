package com.latiendita.customerService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.latiendita.customerService.customer.CustomerCreation;
import com.latiendita.customerService.customer.CustomerEntity;
import com.latiendita.customerService.customer.CustomerRepository;
import com.latiendita.customerService.customer.CustomerService;
import com.latiendita.customerService.customer.CustomerStatus;
import com.latiendita.customerService.customer.dto.CreateCustomerRequest;
import com.latiendita.customerService.customer.dto.CustomerDto;
import com.latiendita.customerService.customer.dto.CustomerMapper;
import com.latiendita.customerService.customer.dto.CustomerSummaryDto;
import com.latiendita.customerService.customer.dto.NewsletterDto;
import com.latiendita.customerService.customer.dto.NewsletterRequest;
import com.latiendita.customerService.customer.dto.UpdateCustomerRequest;
import com.latiendita.customerService.customer.event.CustomerCreatedEvent;
import com.latiendita.customerService.exception.DuplicateEmailException;
import com.latiendita.customerService.exception.MissingGatewayHeaderException;
import com.latiendita.customerService.outbox.OutboxWriter;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    private static final String SUB = "auth0|abc-123";
    private static final String EMAIL = "ana@mail.com";
    private static final String TOPIC = "customer-events";

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private OutboxWriter outboxWriter;

    @InjectMocks
    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        setGatewayHeaders(SUB, EMAIL);
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void shouldCreateCustomerOnceUsingHeaderSubNotRequestBody() {
        when(customerRepository.findByAuthorizerSubAndDeletedAtIsNull(SUB)).thenReturn(Optional.empty());
        when(customerRepository.existsByEmailAndDeletedAtIsNull(EMAIL)).thenReturn(false);
        when(customerRepository.save(any(CustomerEntity.class))).thenAnswer(invocation -> {
            CustomerEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        CustomerCreation result = customerService.create(
                new CreateCustomerRequest("spoofed-sub", EMAIL, "Ana", "Diaz", "555-1234"));

        assertThat(result.created()).isTrue();
        assertThat(result.customer().authorizerSub()).isEqualTo(SUB);
        assertThat(result.customer().email()).isEqualTo(EMAIL);
        assertThat(result.customer().status()).isEqualTo(CustomerStatus.ACTIVE);
        assertThat(result.customer().newsletterOptIn()).isFalse();
        assertThat(result.customer().newsletterConsentAt()).isNull();
        verify(outboxWriter).append(eq(TOPIC), anyString(), eq("CUSTOMER_CREATED"),
                any(CustomerCreatedEvent.class));
    }

    @Test
    void shouldBeIdempotentOnSecondCreateWithoutPublishingAgain() {
        CustomerEntity existing = existingCustomer();
        when(customerRepository.findByAuthorizerSubAndDeletedAtIsNull(SUB)).thenReturn(Optional.of(existing));

        CustomerCreation result = customerService.create(
                new CreateCustomerRequest(SUB, EMAIL, "Ana", "Diaz", "555-1234"));

        assertThat(result.created()).isFalse();
        assertThat(result.customer().id()).isEqualTo(existing.getId());
        assertThat(result.customer().authorizerSub()).isEqualTo(SUB);
        verify(customerRepository, never()).save(any());
        verifyNoInteractions(outboxWriter);
    }

    @Test
    void shouldRejectCreationWhenHeaderIsMissing() {
        RequestContextHolder.resetRequestAttributes();

        assertThatThrownBy(() -> customerService.create(
                new CreateCustomerRequest(SUB, EMAIL, "Ana", "Diaz", null)))
                .isInstanceOf(MissingGatewayHeaderException.class);
        verifyNoInteractions(customerRepository, outboxWriter);
    }

    @Test
    void shouldReclaimOrphanedCustomerWhenSubChangesAfterAccountRecreation() {
        CustomerEntity orphan = existingCustomer();
        orphan.setAuthorizerSub("auth0|old-sub");
        String newSub = CurrentUser.id();

        when(customerRepository.findByAuthorizerSubAndDeletedAtIsNull(newSub)).thenReturn(Optional.empty());
        when(customerRepository.existsByEmailAndDeletedAtIsNull(EMAIL)).thenReturn(true);
        when(customerRepository.findByEmailAndDeletedAtIsNull(EMAIL)).thenReturn(Optional.of(orphan));
        when(customerRepository.save(orphan)).thenReturn(orphan);

        CustomerCreation result = customerService.create(
                new CreateCustomerRequest(newSub, EMAIL, "Ana", "Diaz", "555-1234"));

        assertThat(result.created()).isFalse();
        assertThat(result.customer().id()).isEqualTo(orphan.getId());
        assertThat(orphan.getAuthorizerSub()).isEqualTo(newSub);
        verifyNoInteractions(outboxWriter);
    }

    @Test
    void shouldNotReclaimFromBodyEmailWhenGatewayDidNotSendTrustedClaim() {
        setGatewayHeaders("auth0|other-456", null);
        when(customerRepository.findByAuthorizerSubAndDeletedAtIsNull("auth0|other-456"))
                .thenReturn(Optional.empty());
        when(customerRepository.existsByEmailAndDeletedAtIsNull(EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> customerService.create(
                new CreateCustomerRequest("auth0|other-456", EMAIL, "Ana", "Diaz", null)))
                .isInstanceOf(DuplicateEmailException.class);
        verify(customerRepository, never()).findByEmailAndDeletedAtIsNull(anyString());
        verify(customerRepository, never()).save(any());
    }

    @Test
    void shouldRecordConsentOnOptInAndClearItImmediatelyOnOptOut() {
        CustomerEntity customer = existingCustomer();
        when(customerRepository.findByAuthorizerSubAndDeletedAtIsNull(SUB)).thenReturn(Optional.of(customer));
        when(customerRepository.save(customer)).thenReturn(customer);

        NewsletterDto optedIn = customerService.updateNewsletter(new NewsletterRequest(true));

        assertThat(optedIn.newsletterOptIn()).isTrue();
        assertThat(optedIn.newsletterConsentAt()).isNotNull();
        assertThat(customer.getNewsletterConsentAt()).isNotNull();

        NewsletterDto optedOut = customerService.updateNewsletter(new NewsletterRequest(false));

        assertThat(optedOut.newsletterOptIn()).isFalse();
        assertThat(optedOut.newsletterConsentAt()).isNull();
        assertThat(customer.getNewsletterConsentAt()).isNull();
    }

    @Test
    void shouldUpdateContactInformation() {
        CustomerEntity customer = existingCustomer();
        when(customerRepository.findByAuthorizerSubAndDeletedAtIsNull(SUB)).thenReturn(Optional.of(customer));
        when(customerRepository.save(customer)).thenReturn(customer);

        CustomerDto updated = customerService.updateMe(
                new UpdateCustomerRequest("Ana Maria", "Diaz Ruiz", "555-9999"));

        assertThat(updated.firstName()).isEqualTo("Ana Maria");
        assertThat(updated.lastName()).isEqualTo("Diaz Ruiz");
        assertThat(updated.phone()).isEqualTo("555-9999");
    }

    @Test
    void shouldValidateUpdateRequestConstraints() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        Set<String> invalidFields = validator.validate(new UpdateCustomerRequest(" ", "Diaz", null))
                .stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertThat(invalidFields).contains("firstName");
        assertThat(validator.validate(new UpdateCustomerRequest("Ana", "Diaz", null))).isEmpty();
    }

    @Test
    void shouldProjectReducedCustomerSummary() {
        CustomerSummaryDto summary = CustomerMapper.toSummaryDto(existingCustomer());

        assertThat(summary.id()).isEqualTo(existingCustomer().getId());
        assertThat(summary.firstName()).isEqualTo("Ana");
        assertThat(summary.lastName()).isEqualTo("Diaz");
        assertThat(summary.email()).isEqualTo(EMAIL);
        assertThat(summary.status()).isEqualTo(CustomerStatus.ACTIVE);
        assertThat(summary.lastLoginAt()).isNotNull();
    }

    private void setGatewayHeaders(String sub, String email) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CurrentUser.ID_HEADER, sub);
        if (email != null) {
            request.addHeader(CurrentUser.EMAIL_HEADER, email);
        }
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private CustomerEntity existingCustomer() {
        Instant now = Instant.parse("2026-11-19T12:00:00Z");
        return CustomerEntity.builder()
                .id(UUID.fromString("3f2504e0-4f89-11d3-9a0c-0305e82c3301"))
                .authorizerSub(SUB)
                .email(EMAIL)
                .firstName("Ana")
                .lastName("Diaz")
                .phone("555-1234")
                .status(CustomerStatus.ACTIVE)
                .newsletterOptIn(false)
                .lastLoginAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
