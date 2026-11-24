package com.latiendita.customerService.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.latiendita.customerService.customer.CustomerEntity;
import com.latiendita.customerService.customer.CustomerRepository;
import com.latiendita.customerService.customer.CustomerStatus;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class UserRegisteredConsumerTest {

    private static final String SUB = "auth0|abc-123";
    private static final String EMAIL = "ana@mail.com";
    private static final String REGISTERED = """
            {"type":"USER_REGISTERED","sub":"auth0|abc-123","email":"ana@mail.com",\
            "firstName":"Ana","lastName":"Diaz","occurredAt":"2026-11-19T12:00:00Z"}
            """;

    @Mock
    private CustomerRepository customerRepository;

    private UserRegisteredConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new UserRegisteredConsumer(customerRepository, new ObjectMapper());
    }

    @Test
    void shouldCreateCustomerProfileFromUserRegisteredEvent() {
        when(customerRepository.findByAuthorizerSubAndDeletedAtIsNull(SUB)).thenReturn(Optional.empty());
        when(customerRepository.save(any(CustomerEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        consumer.onUserEvent(REGISTERED);

        ArgumentCaptor<CustomerEntity> captor = ArgumentCaptor.forClass(CustomerEntity.class);
        verify(customerRepository).save(captor.capture());
        CustomerEntity saved = captor.getValue();
        assertThat(saved.getAuthorizerSub()).isEqualTo(SUB);
        assertThat(saved.getEmail()).isEqualTo(EMAIL);
        assertThat(saved.getFirstName()).isEqualTo("Ana");
        assertThat(saved.getLastName()).isEqualTo("Diaz");
        assertThat(saved.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
        assertThat(saved.isNewsletterOptIn()).isFalse();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldBeIdempotentWhenProfileAlreadyExists() {
        when(customerRepository.findByAuthorizerSubAndDeletedAtIsNull(SUB))
                .thenReturn(Optional.of(CustomerEntity.builder().authorizerSub(SUB).build()));

        consumer.onUserEvent(REGISTERED);

        verify(customerRepository, never()).save(any());
    }

    @Test
    void shouldIgnoreUnrelatedEventTypes() {
        consumer.onUserEvent("""
                {"type":"USER_UPDATED","sub":"auth0|abc-123","email":"ana@mail.com"}
                """);

        verify(customerRepository, never()).findByAuthorizerSubAndDeletedAtIsNull(anyString());
        verify(customerRepository, never()).save(any());
    }

    @Test
    void shouldDiscardMalformedJsonWithoutFailing() {
        consumer.onUserEvent("esto no es json {{{");
        consumer.onUserEvent("");

        verify(customerRepository, never()).findByAuthorizerSubAndDeletedAtIsNull(anyString());
        verify(customerRepository, never()).save(any());
    }
}
